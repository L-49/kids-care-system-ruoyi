<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb8">
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">教职工总数</div>
          <div class="mini-value">{{ overview.total || 0 }}<span class="unit">人</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">在职人数</div>
          <div class="mini-value">{{ overview.active || 0 }}<span class="unit">人</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card" :class="{ danger: (overview.inactive || 0) > 0 }">
          <div class="mini-label">离职人数</div>
          <div class="mini-value">{{ overview.inactive || 0 }}<span class="unit">人</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">担任班主任</div>
          <div class="mini-value">{{ overview.headTeachers || 0 }}<span class="unit">人</span></div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="岗位" prop="role">
        <el-select v-model="queryParams.role" placeholder="全部岗位" clearable style="width: 140px">
          <el-option v-for="item in roleOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="在职状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="在职" :value="1" />
          <el-option label="离职" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:teacher:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:teacher:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:teacher:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="teacherList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="姓名" align="center" prop="name" width="100" />
      <el-table-column label="性别" align="center" prop="gender" width="70" />
      <el-table-column label="联系电话" align="center" prop="phone" width="130" />
      <el-table-column label="岗位" align="center" prop="role" width="100" />
      <el-table-column label="职称" align="center" prop="title" width="110" />
      <el-table-column label="学历" align="center" prop="education" width="90" />
      <el-table-column label="身份证号" align="center" prop="idCard" width="180" />
      <el-table-column label="入职日期" align="center" prop="entryDate" width="120" />
      <el-table-column label="紧急联系人" align="center" prop="emergencyContact" width="110" />
      <el-table-column label="紧急电话" align="center" prop="emergencyPhone" width="130" />
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '在职' : '离职' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="OfficeBuilding" @click="openClasses(scope.row)">任教班级</el-button>
          <el-button link type="success" icon="Notebook" @click="openCourses(scope.row)">任教课程</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:teacher:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:teacher:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="teacherRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="女">女</el-radio>
                <el-radio value="男">男</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="role">
              <el-select v-model="form.role" placeholder="请选择岗位" style="width: 100%">
                <el-option v-for="item in roleOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-input v-model="form.title" placeholder="如 一级教师" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历" prop="education">
              <el-select v-model="form.education" placeholder="请选择学历" clearable style="width: 100%">
                <el-option v-for="item in educationOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="18 位身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryDate">
              <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系人" prop="emergencyContact">
              <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急电话" prop="emergencyPhone">
              <el-input v-model="form.emergencyPhone" placeholder="请输入紧急电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在职状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">在职</el-radio>
                <el-radio :value="0">离职</el-radio>
              </el-radio-group>
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

    <!-- 任教班级 -->
    <el-dialog v-model="clazzVisible" :title="currentTeacher.name + ' · 任教班级'" width="780px" append-to-body destroy-on-close>
      <div style="margin-bottom: 10px">
        <el-button type="primary" size="small" icon="Plus" @click="openAssign" v-hasPermi="['kids:teacher:edit']">设为班主任</el-button>
      </div>
      <el-table v-loading="clazzLoading" :data="clazzRows" max-height="360">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="clazzName" label="班级" width="110" />
        <el-table-column label="来源" width="130" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isHead ? 'success' : 'info'" size="small" effect="plain">{{ scope.row.source }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="room" label="教室" width="100" />
        <el-table-column prop="capacity" label="额定人数" width="100" align="center" />
        <el-table-column prop="childCount" label="在园人数" width="100" align="center" />
        <el-table-column label="操作" width="130" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.isHead" link type="danger" @click="cancelHead(scope.row)" v-hasPermi="['kids:teacher:edit']">取消班主任</el-button>
            <span v-else style="color: #c0c4cc">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 设为班主任 -->
    <el-dialog v-model="assignVisible" :title="'设为班主任 · ' + currentTeacher.name" width="440px" append-to-body destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="选择班级" required>
          <el-select v-model="assignClazzId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="c in clazzOptions"
              :key="c.id"
              :value="c.id"
              :label="c.headTeacherId && c.headTeacherId !== currentTeacher.id ? c.name + '（现任：' + (c.headTeacherName || '其他教职工') + '）' : c.name"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="assignSaving" @click="submitAssign">确 定</el-button>
          <el-button @click="assignVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任教课程 -->
    <el-dialog v-model="courseVisible" :title="currentTeacher.name + ' · 任教课程'" width="880px" append-to-body destroy-on-close>
      <div style="margin-bottom: 10px">
        <el-button type="primary" size="small" icon="Plus" @click="openCourseForm()" v-hasPermi="['kids:course:add']">新增课程</el-button>
      </div>
      <el-table v-loading="courseLoading" :data="courses" max-height="380">
        <el-table-column prop="title" label="课程" min-width="130" />
        <el-table-column prop="clazzName" label="班级" width="110" />
        <el-table-column prop="courseDate" label="日期" width="120" />
        <el-table-column label="时间" width="140" align="center">
          <template #default="scope">{{ scope.row.startTime || '--' }} ~ {{ scope.row.endTime || '--' }}</template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openCourseForm(scope.row)" v-hasPermi="['kids:course:edit']">编辑</el-button>
            <el-button link type="danger" @click="removeCourse(scope.row)" v-hasPermi="['kids:course:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 课程表单 -->
    <el-dialog v-model="courseFormVisible" :title="courseForm.id ? '编辑课程' : '新增课程'" width="560px" append-to-body destroy-on-close>
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseRules" label-width="90px">
        <el-form-item label="课程名称" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="班级" prop="clazzId">
          <el-select v-model="courseForm.clazzId" placeholder="请选择班级" style="width: 100%">
            <el-option v-for="c in clazzOptions" :key="c.id" :value="c.id" :label="c.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师">
          <span>{{ currentTeacher.name }}</span>
        </el-form-item>
        <el-form-item label="上课日期" prop="courseDate">
          <el-date-picker v-model="courseForm.courseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="courseForm.startTime" format="HH:mm" value-format="HH:mm" placeholder="如 09:00" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="courseForm.endTime" format="HH:mm" value-format="HH:mm" placeholder="如 09:40" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动内容">
          <el-input v-model="courseForm.content" type="textarea" :rows="3" placeholder="如：绘本共读与表演" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="courseSaving" @click="submitCourse">确 定</el-button>
          <el-button @click="courseFormVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="KidsTeacher">
import { listTeacher, getTeacher, addTeacher, updateTeacher, delTeacher, teacherOverview, teacherClasses, teacherCourses, setHeadTeacher, removeHeadTeacher } from '@/api/kids/teacher'
import { listClazz } from '@/api/kids/clazz'
import { addCourse, updateCourse, delCourse } from '@/api/kids/course'

const { proxy } = getCurrentInstance()

const roleOptions = ['园长', '副园长', '教师', '保育员', '保健医', '厨师', '保安']
const educationOptions = ['研究生', '本科', '大专', '中专', '高中']

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
  queryParams: { pageNum: 1, pageSize: 10, name: null, role: null, status: null },
  rules: {
    name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    phone: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listTeacher(queryParams.value).then((res) => {
    teacherList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function loadOverview() {
  teacherOverview().then((res) => {
    overview.value = res.data || {}
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, name: null, gender: '女', phone: null, role: '教师', title: null, education: null, idCard: null, emergencyContact: null, emergencyPhone: null, entryDate: null, status: 1 }
  proxy.resetForm('teacherRef')
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
  title.value = '新增教职工'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getTeacher(id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改教职工'
  })
}

function submitForm() {
  proxy.$refs['teacherRef'].validate((valid) => {
    if (!valid) return
    if (form.value.id != null) {
      updateTeacher(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
        loadOverview()
      })
    } else {
      addTeacher(form.value).then(() => {
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
  proxy.$modal.confirm('担任班主任的教职工需先取消班主任后才能删除，是否继续？').then(() => {
    return delTeacher(delIds)
  }).then(() => {
    getList()
    loadOverview()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// ---------------- 任教班级 ----------------
const clazzVisible = ref(false)
const clazzLoading = ref(false)
const clazzRows = ref([])
const currentTeacher = ref({})
const clazzOptions = ref([])

function openClasses(row) {
  currentTeacher.value = row
  clazzVisible.value = true
  loadClasses()
  loadClazzOptions()
}

function loadClasses() {
  clazzLoading.value = true
  teacherClasses(currentTeacher.value.id).then((res) => {
    clazzRows.value = res.data || []
    clazzLoading.value = false
  }).catch(() => {
    clazzLoading.value = false
  })
}

function loadClazzOptions() {
  listClazz({ pageNum: 1, pageSize: 100 }).then((res) => {
    clazzOptions.value = res.rows || []
  })
}

const assignVisible = ref(false)
const assignSaving = ref(false)
const assignClazzId = ref(null)

function openAssign() {
  assignClazzId.value = null
  assignVisible.value = true
}

function submitAssign() {
  if (!assignClazzId.value) {
    proxy.$modal.msgWarning('请选择班级')
    return
  }
  const target = clazzOptions.value.find((c) => c.id === assignClazzId.value)
  const doAssign = () => {
    assignSaving.value = true
    setHeadTeacher(currentTeacher.value.id, assignClazzId.value, true).then(() => {
      proxy.$modal.msgSuccess('已设为班主任')
      assignSaving.value = false
      assignVisible.value = false
      loadClasses()
      loadOverview()
      getList()
    }).catch(() => {
      assignSaving.value = false
    })
  }
  if (target && target.headTeacherId && target.headTeacherId !== currentTeacher.value.id) {
    proxy.$modal.confirm('班级【' + target.name + '】现任班主任为【' + (target.headTeacherName || '其他教职工') + '】，确定替换吗？').then(doAssign).catch(() => {})
  } else {
    doAssign()
  }
}

function cancelHead(row) {
  proxy.$modal.confirm('确定取消【' + currentTeacher.value.name + '】在【' + row.clazzName + '】的班主任职务吗？').then(() => {
    return removeHeadTeacher(currentTeacher.value.id, row.clazzId)
  }).then(() => {
    proxy.$modal.msgSuccess('已取消班主任')
    loadClasses()
    loadOverview()
    getList()
  }).catch(() => {})
}

// ---------------- 任教课程 ----------------
const courseVisible = ref(false)
const courseLoading = ref(false)
const courses = ref([])

function openCourses(row) {
  currentTeacher.value = row
  courseVisible.value = true
  loadCourses()
  loadClazzOptions()
}

function loadCourses() {
  courseLoading.value = true
  teacherCourses(currentTeacher.value.id).then((res) => {
    courses.value = res.data || []
    courseLoading.value = false
  }).catch(() => {
    courseLoading.value = false
  })
}

const courseFormVisible = ref(false)
const courseSaving = ref(false)
const courseFormRef = ref()
const courseForm = reactive({})
const courseRules = {
  title: [{ required: true, message: '请填写课程名称', trigger: 'blur' }],
  clazzId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  courseDate: [{ required: true, message: '请选择上课日期', trigger: 'change' }]
}

function openCourseForm(row) {
  Object.keys(courseForm).forEach((k) => delete courseForm[k])
  if (row) {
    Object.assign(courseForm, JSON.parse(JSON.stringify(row)))
  } else {
    Object.assign(courseForm, { teacherId: currentTeacher.value.id, status: 1 })
  }
  courseFormVisible.value = true
}

function submitCourse() {
  if (!courseFormRef.value) return
  courseFormRef.value.validate((valid) => {
    if (!valid) return
    courseSaving.value = true
    const payload = { ...courseForm, teacherId: currentTeacher.value.id }
    const action = payload.id ? updateCourse(payload) : addCourse(payload)
    action.then(() => {
      proxy.$modal.msgSuccess(payload.id ? '修改成功' : '新增成功')
      courseSaving.value = false
      courseFormVisible.value = false
      loadCourses()
    }).catch(() => {
      courseSaving.value = false
    })
  })
}

function removeCourse(row) {
  proxy.$modal.confirm('确定删除课程「' + row.title + '」吗？').then(() => {
    return delCourse(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    loadCourses()
  }).catch(() => {})
}

onMounted(() => {
  getList()
  loadOverview()
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
