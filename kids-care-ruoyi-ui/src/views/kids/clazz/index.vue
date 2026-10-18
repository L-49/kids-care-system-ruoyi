<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb8">
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">班级总数</div>
          <div class="mini-value">{{ overview.clazzCount || 0 }}<span class="unit">个</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">在园幼儿</div>
          <div class="mini-value">{{ overview.childCount || 0 }}<span class="unit">人</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">整体满员率</div>
          <div class="mini-value">{{ overview.rate || 0 }}<span class="unit">%</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card" :class="{ danger: (overview.overCount || 0) > 0 }">
          <div class="mini-label">超额班级</div>
          <div class="mini-value">{{ overview.overCount || 0 }}<span class="unit">个</span></div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="班级名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入班级名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:clazz:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:clazz:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:clazz:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="clazzList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="班级名称" align="center" prop="name" width="120" />
      <el-table-column label="适龄段" align="center" prop="ageRange" width="100" />
      <el-table-column label="教室" align="center" prop="room" width="100" />
      <el-table-column label="额定人数" align="center" prop="capacity" width="100" />
      <el-table-column label="在园人数" align="center" prop="childCount" width="100" />
      <el-table-column label="满员状态" align="center" width="110">
        <template #default="scope">
          <el-tag :type="fullTag(scope.row.fullStatus)">{{ scope.row.fullStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开班日期" align="center" prop="openDate" width="120" />
      <el-table-column label="班主任" align="center" prop="headTeacherName" width="110" />
      <el-table-column label="班级特色" align="center" prop="remark" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="openChildren(scope.row)">幼儿名单</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:clazz:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:clazz:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改班级 -->
    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="clazzRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="班级名称" prop="name">
              <el-input v-model="form.name" placeholder="如 小班" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适龄段" prop="ageRange">
              <el-input v-model="form.ageRange" placeholder="如 3-4岁" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教室" prop="room">
              <el-input v-model="form.room" placeholder="如 A101" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="额定人数" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" :max="60" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开班日期" prop="openDate">
              <el-date-picker v-model="form.openDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班主任" prop="headTeacherId">
              <el-select v-model="form.headTeacherId" placeholder="请选择班主任" clearable style="width: 100%">
                <el-option v-for="item in teacherList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="班级特色" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入班级特色或备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 幼儿名单 -->
    <el-dialog v-model="childVisible" :title="currentClazz.name + ' · 幼儿名单'" width="820px" append-to-body destroy-on-close>
      <el-table v-loading="childLoading" :data="children" max-height="420">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="name" label="姓名" width="110" />
        <el-table-column prop="gender" label="性别" width="70" align="center" />
        <el-table-column prop="guardianName" label="家长" width="110" />
        <el-table-column prop="guardianPhone" label="家长电话" width="140" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="在园"
              inline-prompt
              @change="toggleChildStatus(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openTransfer(scope.row)">调班</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 调班 -->
    <el-dialog v-model="transferVisible" :title="'调班 · ' + (transferRow ? transferRow.name : '')" width="440px" append-to-body destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="现班级">
          <span>{{ currentClazz.name }}</span>
        </el-form-item>
        <el-form-item label="调入班级" required>
          <el-select v-model="transferTarget" placeholder="请选择目标班级" style="width: 100%">
            <el-option
              v-for="c in clazzOptions"
              :key="c.id"
              :value="c.id"
              :label="c.name + '（' + (c.childCount || 0) + '/' + c.capacity + ' 人）'"
              :disabled="c.id === currentClazz.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="transferSaving" @click="submitTransfer">确 定</el-button>
          <el-button @click="transferVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="KidsClazz">
import { listClazz, getClazz, addClazz, updateClazz, delClazz, clazzOverview, clazzChildren } from '@/api/kids/clazz'
import { listTeacherAll } from '@/api/kids/teacher'
import { updateChild, transferChild } from '@/api/kids/child'

const { proxy } = getCurrentInstance()

const clazzList = ref([])
const teacherList = ref([])
const overview = ref({})
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
  queryParams: { pageNum: 1, pageSize: 10, name: null },
  rules: {
    name: [{ required: true, message: '班级名称不能为空', trigger: 'blur' }],
    capacity: [{ required: true, message: '额定人数不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function fullTag(status) {
  return { 正常: 'success', 已满: 'warning', 超额: 'danger' }[status] || 'info'
}

function getList() {
  loading.value = true
  listClazz(queryParams.value).then((res) => {
    clazzList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function loadOverview() {
  clazzOverview().then((res) => {
    overview.value = res.data || {}
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, name: null, ageRange: null, room: null, capacity: 25, headTeacherId: null, openDate: null, remark: null }
  proxy.resetForm('clazzRef')
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
  title.value = '新增班级'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getClazz(id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改班级'
  })
}

function submitForm() {
  proxy.$refs['clazzRef'].validate((valid) => {
    if (!valid) return
    if (form.value.id != null) {
      updateClazz(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
        loadOverview()
      })
    } else {
      addClazz(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
        loadOverview()
      })
    }
  })
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('删除前请确认该班级没有在园幼儿，是否继续？').then(() => {
    return delClazz(delIds)
  }).then(() => {
    getList()
    loadOverview()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// ---------------- 幼儿名单 ----------------
const childVisible = ref(false)
const childLoading = ref(false)
const children = ref([])
const currentClazz = ref({})
const clazzOptions = ref([])

function openChildren(row) {
  currentClazz.value = row
  childVisible.value = true
  childLoading.value = true
  Promise.all([clazzChildren(row.id), listClazz({ pageNum: 1, pageSize: 100 })]).then(([childRes, clazzRes]) => {
    children.value = childRes.data || []
    clazzOptions.value = clazzRes.rows || []
    childLoading.value = false
  }).catch(() => {
    childLoading.value = false
  })
}

function toggleChildStatus(row) {
  const going = row.status === 1
  const old = going ? 0 : 1
  proxy.$modal.confirm('确定将「' + row.name + '」标记为' + (going ? '在园' : '离园') + '吗？').then(() => {
    return updateChild({ id: row.id, status: row.status })
  }).then(() => {
    proxy.$modal.msgSuccess('状态已更新')
    loadOverview()
    getList()
  }).catch(() => {
    row.status = old
  })
}

// ---------------- 调班 ----------------
const transferVisible = ref(false)
const transferSaving = ref(false)
const transferTarget = ref(null)
const transferRow = ref(null)

function openTransfer(row) {
  transferRow.value = row
  transferTarget.value = null
  transferVisible.value = true
}

function submitTransfer() {
  if (!transferTarget.value) {
    proxy.$modal.msgWarning('请选择要调入的班级')
    return
  }
  transferSaving.value = true
  transferChild(transferRow.value.id, transferTarget.value).then(() => {
    proxy.$modal.msgSuccess('调班成功')
    transferVisible.value = false
    transferSaving.value = false
    return Promise.all([clazzChildren(currentClazz.value.id), loadOverview()])
  }).then((res) => {
    if (res && res[0]) {
      children.value = res[0].data || []
    }
    getList()
  }).catch(() => {
    transferSaving.value = false
  })
}

onMounted(() => {
  getList()
  loadOverview()
  listTeacherAll().then((res) => {
    teacherList.value = (res.data || []).map((t) => ({ id: t.id, name: t.name + '（' + (t.role || '') + '）' }))
  })
})
</script>

<style scoped>
.mini-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 18px;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.06);
  border-left: 3px solid #409eff;
}

.mini-card.danger {
  border-left-color: #f56c6c;
}

.mini-label {
  font-size: 13px;
  color: #909399;
}

.mini-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.mini-value .unit {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
  margin-left: 4px;
}
</style>
