<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="课程名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入课程名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="班级" prop="clazzId">
        <el-select v-model="queryParams.clazzId" placeholder="全部班级" clearable style="width: 160px">
          <el-option v-for="item in clazzList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="上课日期" prop="courseDate">
        <el-date-picker v-model="queryParams.courseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:course:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:course:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:course:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="courseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="日期" align="center" prop="courseDate" width="120" />
      <el-table-column label="开始" align="center" prop="startTime" width="90" />
      <el-table-column label="结束" align="center" prop="endTime" width="90" />
      <el-table-column label="课程名称" align="center" prop="title" :show-overflow-tooltip="true" min-width="140" />
      <el-table-column label="班级" align="center" prop="clazzName" width="110" />
      <el-table-column label="授课教师" align="center" prop="teacherName" width="110" />
      <el-table-column label="活动内容" align="center" prop="content" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '正常' : '取消' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:course:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:course:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="courseRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级" prop="clazzId">
              <el-select v-model="form.clazzId" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="item in clazzList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select v-model="form.teacherId" placeholder="请选择教师" clearable style="width: 100%">
                <el-option v-for="item in teacherList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课日期" prop="courseDate">
              <el-date-picker v-model="form.courseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-input v-model="form.startTime" placeholder="如 09:00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-input v-model="form.endTime" placeholder="如 09:40" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">取消</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="活动内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入活动内容" />
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

<script setup name="KidsCourse">
import { listCourse, getCourse, addCourse, updateCourse, delCourse } from '@/api/kids/course'
import { listClazz } from '@/api/kids/clazz'
import { listTeacherAll } from '@/api/kids/teacher'

const { proxy } = getCurrentInstance()

const courseList = ref([])
const clazzList = ref([])
const teacherList = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, title: null, clazzId: null, courseDate: null },
  rules: {
    title: [{ required: true, message: '课程名称不能为空', trigger: 'blur' }],
    clazzId: [{ required: true, message: '请选择班级', trigger: 'change' }],
    courseDate: [{ required: true, message: '上课日期不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCourse(queryParams.value).then((res) => {
    courseList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { status: 1, clazzId: null, teacherId: null, title: null, courseDate: null, startTime: null, endTime: null, content: null, id: null }
  proxy.resetForm('courseRef')
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
  title.value = '新增课程'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getCourse(id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改课程'
  })
}

function submitForm() {
  proxy.$refs['courseRef'].validate((valid) => {
    if (!valid) return
    if (form.value.id != null) {
      updateCourse(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addCourse(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的课程记录？').then(() => {
    return delCourse(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

onMounted(() => {
  getList()
  listClazz({ pageNum: 1, pageSize: 100 }).then((res) => { clazzList.value = res.rows || [] })
  listTeacherAll().then((res) => { teacherList.value = res.data || [] })
})
</script>
