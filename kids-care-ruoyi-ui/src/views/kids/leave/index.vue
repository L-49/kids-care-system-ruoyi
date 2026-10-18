<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="审批状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option label="待审批" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker v-model="queryParams.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:leave:add']">申请请假</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:leave:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:leave:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="leaveList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="幼儿" align="center" prop="childName" width="110" />
      <el-table-column label="开始日期" align="center" prop="startDate" width="120" />
      <el-table-column label="结束日期" align="center" prop="endDate" width="120" />
      <el-table-column label="天数" align="center" prop="days" width="80" />
      <el-table-column label="请假事由" align="center" prop="reason" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批人" align="center" prop="approver" width="110" />
      <el-table-column label="操作" align="center" width="190" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" link type="primary" icon="Select" @click="handleApprove(scope.row, true)" v-hasPermi="['kids:leave:approve']">通过</el-button>
          <el-button v-if="scope.row.status === 0" link type="danger" icon="Close" @click="handleApprove(scope.row, false)" v-hasPermi="['kids:leave:approve']">驳回</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:leave:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="leaveRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="幼儿" prop="childId">
          <el-select v-model="form.childId" placeholder="请选择幼儿" filterable style="width: 100%">
            <el-option v-for="item in childList" :key="item.id" :label="item.name + '（' + (item.clazzName || '未分班') + '）'" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="请假日期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="天数" prop="days">
          <el-input-number v-model="form.days" :min="1" :max="60" />
          <span style="margin-left: 8px; color: #909399">留空则按起止日期自动计算</span>
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入请假事由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="KidsLeave">
import { listLeave, getLeave, addLeave, updateLeave, delLeave, approveLeave } from '@/api/kids/leave'
import { listChildAll } from '@/api/kids/child'

const { proxy } = getCurrentInstance()

const leaveList = ref([])
const childList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, status: null, startDate: null },
  rules: {
    childId: [{ required: true, message: '请选择幼儿', trigger: 'change' }],
    dateRange: [{ required: true, message: '请选择请假日期', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function statusLabel(status) {
  return { 0: '待审批', 1: '已通过', 2: '已驳回' }[status] || '未知'
}

function statusTag(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info'
}

function getList() {
  loading.value = true
  listLeave(queryParams.value).then((res) => {
    leaveList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, childId: null, dateRange: [], days: null, reason: null }
  proxy.resetForm('leaveRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '申请请假'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getLeave(id).then((res) => {
    const data = res.data || {}
    form.value = { ...data, dateRange: data.startDate && data.endDate ? [data.startDate, data.endDate] : [] }
    open.value = true
    title.value = '修改请假'
  })
}

function submitForm() {
  proxy.$refs['leaveRef'].validate((valid) => {
    if (!valid) return
    const payload = { ...form.value }
    payload.startDate = payload.dateRange && payload.dateRange[0]
    payload.endDate = payload.dateRange && payload.dateRange[1]
    delete payload.dateRange
    if (payload.id != null) {
      updateLeave(payload).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addLeave(payload).then(() => {
        proxy.$modal.msgSuccess('申请已提交')
        open.value = false
        getList()
      })
    }
  })
}

function handleApprove(row, pass) {
  const tip = pass ? '确认通过该请假申请？' : '确认驳回该请假申请？'
  proxy.$modal.confirm(tip).then(() => {
    return approveLeave(row.id, pass)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess(pass ? '已通过' : '已驳回')
  }).catch(() => {})
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的请假记录？').then(() => {
    return delLeave(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

onMounted(() => {
  getList()
  listChildAll().then((res) => { childList.value = res.data || [] })
})
</script>
