<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="幼儿姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所在班级" prop="clazzId">
        <el-select v-model="queryParams.clazzId" placeholder="全部班级" clearable style="width: 160px">
          <el-option v-for="item in clazzList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="在园状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="在园" :value="1" />
          <el-option label="离园" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="档案完整度" prop="incompleteOnly">
        <el-select v-model="queryParams.incompleteOnly" placeholder="全部" clearable style="width: 150px">
          <el-option label="仅看待补全" :value="true" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:child:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:child:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:child:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" :loading="importing" @click="handleImport" v-hasPermi="['kids:child:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain icon="Download" @click="downloadTemplate" v-hasPermi="['kids:child:import']">下载模板</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['kids:child:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="childList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="姓名" align="center" prop="name" width="100" />
      <el-table-column label="性别" align="center" prop="gender" width="70" />
      <el-table-column label="出生日期" align="center" prop="birthday" width="120" />
      <el-table-column label="所在班级" align="center" prop="clazzName" width="100" />
      <el-table-column label="家长姓名" align="center" prop="guardianName" width="100" />
      <el-table-column label="家长电话" align="center" prop="guardianPhone" width="130" />
      <el-table-column label="过敏史" align="center" prop="allergy" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="档案完整度" align="center" width="150">
        <template #default="scope">
          <el-progress :percentage="scope.row.completeness || 0" :stroke-width="12" :color="progressColor(scope.row.completeness)" />
        </template>
      </el-table-column>
      <el-table-column label="待补全项" align="center" min-width="160">
        <template #default="scope">
          <el-tag v-if="scope.row.missingItems" type="warning" size="small">{{ scope.row.missingItems }}</el-tag>
          <el-tag v-else type="success" size="small">已完整</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '在园' : '离园' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Tickets" @click="openHistory(scope.row)">变更记录</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:child:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:child:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="childRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="幼儿姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入幼儿姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" :disabled-date="disabledFuture" style="width: 100%" @change="syncStatus" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在班级" prop="clazzId">
              <el-select v-model="form.clazzId" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="item in clazzList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="家长姓名" prop="guardianName">
              <el-input v-model="form.guardianName" placeholder="请输入家长姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="家长电话" prop="guardianPhone">
              <el-input v-model="form.guardianPhone" placeholder="11 位手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入园日期" prop="enrollDate">
              <el-date-picker v-model="form.enrollDate" type="date" value-format="YYYY-MM-DD" placeholder="可填未来日期，表示预约入园" style="width: 100%" @change="syncStatus" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在园状态" prop="status">
              <el-radio-group v-model="form.status" disabled>
                <el-radio :value="1">在园</el-radio>
                <el-radio :value="0">离园</el-radio>
              </el-radio-group>
              <div class="form-tip">由入园日期自动判定：晚于今天为离园，当天及以前为在园</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="家庭住址" prop="address">
              <el-input v-model="form.address" placeholder="请输入家庭住址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="过敏史/禁忌" prop="allergy">
              <el-input v-model="form.allergy" type="textarea" :rows="2" placeholder="如：牛奶、花生过敏。填写后每周食谱会自动标注该幼儿" />
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

    <!-- 变更记录 -->
    <el-drawer v-model="historyVisible" :title="'变更记录 · ' + historyChild" size="620px">
      <el-timeline v-if="historyList.length">
        <el-timeline-item v-for="h in historyList" :key="h.id" :timestamp="h.createTime" placement="top">
          <div class="hist-item">
            <div class="hist-line">
              <el-tag size="small" :type="histTag(h.changeType)">{{ h.changeType }}</el-tag>
              <template v-if="h.fieldLabel">
                <span class="hist-field">{{ h.fieldLabel }}</span>
                <span class="hist-old">{{ h.oldValue || '(空)' }}</span>
                <span class="hist-arrow">→</span>
                <span class="hist-new">{{ h.newValue || '(空)' }}</span>
              </template>
              <span v-else class="hist-field">{{ h.newValue }}</span>
            </div>
            <div class="hist-meta">操作人：{{ h.operator || '系统' }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无变更记录" />
    </el-drawer>

    <!-- 导入 -->
    <el-dialog title="幼儿档案导入" v-model="importVisible" width="460px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx,.xls"
        :auto-upload="false"
        :on-change="handleFileChange"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将 Excel 拖到此处，或<em>点击选择</em></div>
        <template #tip>
          <div class="el-upload__tip">
            请先<el-button link type="primary" @click="downloadTemplate">下载导入模板</el-button>，按模板列填写；班级需与系统中的班级名称一致
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="importing" @click="submitImport">开始导入</el-button>
          <el-button @click="importVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 导入结果 -->
    <el-dialog title="导入结果" v-model="resultVisible" width="660px" append-to-body>
      <el-alert
        :title="'成功 ' + importResult.success + ' 条，失败 ' + importResult.fail + ' 条'"
        :type="importResult.fail > 0 ? 'warning' : 'success'"
        :closable="false"
        show-icon
      />
      <el-table v-if="importResult.errors && importResult.errors.length" :data="importResult.errors" max-height="320" style="margin-top: 12px">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="msg" label="失败原因" min-width="400" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="resultVisible = false">知道了</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="KidsChild">
import { listChild, getChild, addChild, updateChild, delChild, childHistory, importChild } from '@/api/kids/child'
import { listClazz } from '@/api/kids/clazz'

const { proxy } = getCurrentInstance()

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

// ---------------- 日期规则：出生早于入园、入园年龄 3~6 岁、状态由入园日期决定 ----------------
function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

/** 出生日期不允许选未来 */
function disabledFuture(date) {
  return date.getTime() > Date.now()
}

/** 按周岁计算入园时的年龄 */
function ageAt(birth, enroll) {
  const b = new Date(birth)
  const e = new Date(enroll)
  let age = e.getFullYear() - b.getFullYear()
  const m = e.getMonth() - b.getMonth()
  if (m < 0 || (m === 0 && e.getDate() < b.getDate())) age--
  return age
}

function validateBirthday(rule, value, callback) {
  if (!value) return callback(new Error('请选择出生日期'))
  if (value > todayStr()) return callback(new Error('出生日期不能晚于今天'))
  const enroll = form.value.enrollDate
  if (enroll) {
    if (value >= enroll) return callback(new Error('出生日期必须早于入园日期'))
    const age = ageAt(value, enroll)
    if (age < 3 || age > 6) return callback(new Error(`入园时年龄为 ${age} 岁，不符合 3~6 岁入园要求`))
  }
  callback()
}

function validateEnrollDate(rule, value, callback) {
  if (!value) return callback(new Error('请选择入园日期'))
  const birth = form.value.birthday
  if (birth) {
    if (value <= birth) return callback(new Error('入园日期必须晚于出生日期'))
    const age = ageAt(birth, value)
    if (age < 3 || age > 6) return callback(new Error(`入园时年龄为 ${age} 岁，不符合 3~6 岁入园要求`))
  }
  callback()
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, name: null, clazzId: null, status: null, incompleteOnly: null },
  rules: {
    name: [{ required: true, message: '幼儿姓名不能为空', trigger: 'blur' }],
    clazzId: [{ required: true, message: '请选择班级', trigger: 'change' }],
    birthday: [
      { required: true, message: '请选择出生日期', trigger: 'change' },
      { validator: validateBirthday, trigger: 'change' }
    ],
    enrollDate: [
      { required: true, message: '请选择入园日期', trigger: 'change' },
      { validator: validateEnrollDate, trigger: 'change' }
    ],
    guardianPhone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function progressColor(v) {
  if (v >= 90) return '#67c23a'
  if (v >= 60) return '#e6a23c'
  return '#f56c6c'
}

function getList() {
  loading.value = true
  listChild(queryParams.value).then((res) => {
    childList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, name: null, gender: '男', birthday: null, clazzId: null, guardianName: null, guardianPhone: null, address: null, allergy: null, enrollDate: null, status: 1 }
  proxy.resetForm('childRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.incompleteOnly = null
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
  title.value = '新增幼儿档案'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getChild(id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改幼儿档案'
  })
}

/** 入园日期变化时联动状态：晚于今天为离园，当天及以前为在园 */
function syncStatus() {
  form.value.status = form.value.enrollDate && form.value.enrollDate > todayStr() ? 0 : 1
  if (proxy.$refs['childRef']) {
    proxy.$refs['childRef'].validateField(['birthday', 'enrollDate'])
  }
}

function submitForm() {
  // 提交前再算一次，避免只改日期未触发 change 时状态与日期不一致
  form.value.status = form.value.enrollDate && form.value.enrollDate > todayStr() ? 0 : 1
  proxy.$refs['childRef'].validate((valid) => {
    if (!valid) return
    if (form.value.id != null) {
      updateChild(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addChild(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的幼儿档案？').then(() => {
    return delChild(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// ---------------- 变更记录 ----------------
const historyVisible = ref(false)
const historyList = ref([])
const historyChild = ref('')

function openHistory(row) {
  historyChild.value = row.name
  historyList.value = []
  historyVisible.value = true
  childHistory(row.id).then((res) => {
    historyList.value = res.data || []
  })
}

function histTag(type) {
  if (type === '新增') return 'success'
  if (type === '导入') return 'warning'
  return 'primary'
}

// ---------------- 导入导出 ----------------
const importVisible = ref(false)
const importing = ref(false)
const uploadRef = ref()
const importFile = ref(null)
const resultVisible = ref(false)
const importResult = ref({ success: 0, fail: 0, errors: [] })

function handleImport() {
  importFile.value = null
  importVisible.value = true
}

function downloadTemplate() {
  proxy.download('/kids/child/import/template', {}, '幼儿档案导入模板.xlsx')
}

function handleFileChange(uploadFile) {
  importFile.value = uploadFile.raw
}

function submitImport() {
  if (!importFile.value) {
    proxy.$modal.msgWarning('请先选择 Excel 文件')
    return
  }
  importing.value = true
  const formData = new FormData()
  formData.append('file', importFile.value)
  importChild(formData).then((res) => {
    importing.value = false
    importVisible.value = false
    const data = res.data || {}
    const errors = (data.errors || []).map((msg) => ({ msg }))
    importResult.value = { success: data.success || 0, fail: data.fail || 0, errors }
    resultVisible.value = true
    if ((data.success || 0) > 0) {
      getList()
    }
  }).catch(() => {
    importing.value = false
  })
}

function handleExport() {
  proxy.download('/kids/child/export', queryParams.value, '幼儿档案_' + new Date().getTime() + '.xlsx')
}

onMounted(() => {
  getList()
  listClazz({ pageNum: 1, pageSize: 100 }).then((res) => { clazzList.value = res.rows || [] })
})
</script>

<style scoped>
.form-tip {
  width: 100%;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 2px;
}

.hist-item {
  font-size: 13px;
}

.hist-line {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hist-field {
  color: #303133;
  font-weight: 500;
}

.hist-old {
  color: #909399;
  text-decoration: line-through;
}

.hist-arrow {
  color: #c0c4cc;
}

.hist-new {
  color: #303133;
}

.hist-meta {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
