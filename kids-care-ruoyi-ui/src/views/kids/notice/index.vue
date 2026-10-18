<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="70px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题关键字" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 140px">
          <el-option v-for="dict in notice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option label="草稿" :value="0" />
          <el-option label="定时待发" :value="1" />
          <el-option label="已发布" :value="2" />
          <el-option label="已撤回" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:notice:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:notice:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:notice:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="类型" align="center" width="100">
        <template #default="scope">
          <dict-tag :options="notice_type" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="110">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="可见范围" align="center" prop="targetLabel" width="110" />
      <el-table-column label="发布人" align="center" prop="publisher" width="100" />
      <el-table-column label="发布时间" align="center" width="170">
        <template #default="scope">
          <span>{{ scope.row.publishTime || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="内容" align="center" prop="content" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="操作" align="center" width="290" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0 || scope.row.status === 1"
            link
            type="success"
            icon="Promotion"
            @click="handlePublish(scope.row)"
            v-hasPermi="['kids:notice:publish']"
          >立即发布</el-button>
          <el-button
            v-if="scope.row.status === 2"
            link
            type="warning"
            icon="RefreshLeft"
            @click="handleRevoke(scope.row)"
            v-hasPermi="['kids:notice:revoke']"
          >撤回</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:notice:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:notice:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="noticeRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="公告标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入公告标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公告类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                <el-option v-for="dict in notice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布人" prop="publisher">
              <el-input v-model="form.publisher" placeholder="留空则取当前登录人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可见范围" prop="targetType">
              <el-select v-model="form.targetType" style="width: 100%">
                <el-option label="全园" value="all" />
                <el-option label="指定班级" value="class" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.targetType === 'class'" label="指定班级" prop="targetId">
              <el-select v-model="form.targetId" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="c in clazzList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布方式" prop="status">
              <el-select v-model="form.status" style="width: 100%" :disabled="statusLocked">
                <el-option label="保存为草稿" :value="0" />
                <el-option label="定时发布" :value="1" />
                <el-option label="立即发布" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.status === 1">
            <el-form-item label="发布时间" prop="publishTime">
              <el-date-picker
                v-model="form.publishTime"
                type="datetime"
                placeholder="选择发布时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disablePastDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="statusLocked">
            <el-form-item label="发布时间">
              <el-input :model-value="form.publishTime || '—'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公告内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
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
  </div>
</template>

<script setup name="KidsNotice">
import { listNotice, getNotice, addNotice, updateNotice, delNotice, publishNotice, revokeNotice } from '@/api/kids/notice'
import { listClazz } from '@/api/kids/clazz'

const { proxy } = getCurrentInstance()
const { notice_type } = useDict('notice_type')

const noticeList = ref([])
const clazzList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
// 已发布的公告不允许在编辑弹窗里改发布方式
const statusLocked = ref(false)

/** 定时发布时间：不允许选过去的时间 */
function disablePastDate(date) {
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

/** 定时发布时必须选择发布时间 */
function validatePublishTime(rule, value, callback) {
  if (form.value.status === 1 && !value) {
    callback(new Error('定时发布请选择发布时间'))
  } else {
    callback()
  }
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, title: null, type: null, status: null },
  rules: {
    title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
    content: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }],
    publishTime: [{ validator: validatePublishTime, trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function statusText(status) {
  return { 0: '草稿', 1: '定时待发', 2: '已发布', 3: '已撤回' }[status] || '未知'
}

function statusTag(status) {
  return { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info'
}

function getList() {
  loading.value = true
  listNotice(queryParams.value).then((res) => {
    noticeList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  statusLocked.value = false
  form.value = {
    id: null,
    title: null,
    type: '通知',
    content: null,
    publisher: null,
    status: 2,
    publishTime: null,
    targetType: 'all',
    targetId: null
  }
  proxy.resetForm('noticeRef')
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
  title.value = '新增公告'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getNotice(id).then((res) => {
    form.value = res.data
    statusLocked.value = res.data.status === 2
    open.value = true
    title.value = '修改公告'
  })
}

function submitForm() {
  proxy.$refs['noticeRef'].validate((valid) => {
    if (!valid) return
    const payload = { ...form.value }
    if (payload.targetType !== 'class') {
      payload.targetId = null
    }
    // 前端不传发布人时由后端取当前登录人
    if (!payload.publisher) {
      delete payload.publisher
    }
    const action = payload.id != null ? updateNotice(payload) : addNotice(payload)
    action.then(() => {
      proxy.$modal.msgSuccess(payload.id != null ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handlePublish(row) {
  proxy.$modal.confirm('确定立即发布「' + row.title + '」吗？发布后家长端即可看到。').then(() => {
    return publishNotice(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess('已发布')
    getList()
  }).catch(() => {})
}

function handleRevoke(row) {
  proxy.$modal.confirm('确定撤回「' + row.title + '」吗？撤回后不再对外展示，但记录会保留。').then(() => {
    return revokeNotice(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess('已撤回')
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的公告？').then(() => {
    return delNotice(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

onMounted(() => {
  getList()
  listClazz({ pageNum: 1, pageSize: 100 }).then((res) => { clazzList.value = res.rows || [] })
})
</script>
