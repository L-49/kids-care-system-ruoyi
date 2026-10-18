<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="班级" prop="clazzId">
        <el-select v-model="queryParams.clazzId" placeholder="全部班级" clearable style="width: 160px">
          <el-option v-for="item in clazzList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="出勤状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="dict in att_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="考勤日期" prop="attDate">
        <el-date-picker v-model="queryParams.attDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:attendance:add']">登记考勤</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:attendance:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:attendance:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="attendanceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="考勤日期" align="center" prop="attDate" width="120" />
      <el-table-column label="幼儿" align="center" prop="childName" width="110" />
      <el-table-column label="班级" align="center" prop="clazzName" width="110" />
      <el-table-column label="出勤状态" align="center" width="110">
        <template #default="scope">
          <dict-tag :options="att_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="入园时间" align="center" prop="checkInTime" width="110" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:attendance:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:attendance:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="attendanceRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="幼儿" prop="childId">
          <el-select v-model="form.childId" placeholder="请选择幼儿" filterable style="width: 100%" @change="handleChildChange">
            <el-option v-for="item in childList" :key="item.id" :label="item.name + '（' + (item.clazzName || '未分班') + '）'" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤日期" prop="attDate">
          <el-date-picker v-model="form.attDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="出勤状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in att_status" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="入园时间" prop="checkInTime">
          <el-input v-model="form.checkInTime" placeholder="如 08:05" style="width: 200px" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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

<script setup name="KidsAttendance">
import { listAttendance, getAttendance, addAttendance, updateAttendance, delAttendance } from '@/api/kids/attendance'
import { listChildAll } from '@/api/kids/child'
import { listClazz } from '@/api/kids/clazz'

const { proxy } = getCurrentInstance()
const { att_status } = useDict('att_status')

const attendanceList = ref([])
const childList = ref([])
const clazzList = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, clazzId: null, status: null, attDate: null },
  rules: {
    childId: [{ required: true, message: '请选择幼儿', trigger: 'change' }],
    attDate: [{ required: true, message: '考勤日期不能为空', trigger: 'change' }],
    status: [{ required: true, message: '请选择出勤状态', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listAttendance(queryParams.value).then((res) => {
    attendanceList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, childId: null, attDate: null, status: '出勤', checkInTime: null, remark: null }
  proxy.resetForm('attendanceRef')
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
  title.value = '登记考勤'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getAttendance(id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改考勤'
  })
}

function handleChildChange(id) {
  const child = childList.value.find((item) => item.id === id)
  if (child) {
    form.value.clazzId = child.clazzId
  }
}

function submitForm() {
  proxy.$refs['attendanceRef'].validate((valid) => {
    if (!valid) return
    if (form.value.id != null) {
      updateAttendance(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addAttendance(form.value).then(() => {
        proxy.$modal.msgSuccess('登记成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的考勤记录？').then(() => {
    return delAttendance(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

onMounted(() => {
  getList()
  listChildAll().then((res) => { childList.value = res.data || [] })
  listClazz({ pageNum: 1, pageSize: 100 }).then((res) => { clazzList.value = res.rows || [] })
})
</script>
