<template>
  <div class="app-container">
    <el-alert v-if="allergyList.length" type="warning" :closable="false" class="mb8">
      <template #title>
        <span style="font-weight: 600; margin-right: 8px">过敏儿童标注（{{ allergyList.length }} 人，排餐时请避开）</span>
        <el-tag
          v-for="c in allergyList"
          :key="c.id"
          type="danger"
          size="small"
          effect="plain"
          style="margin-right: 6px"
        >{{ c.name }}·{{ c.clazzName || '未分班' }}：{{ c.allergy }}</el-tag>
      </template>
    </el-alert>

    <div class="view-bar">
      <el-radio-group v-model="view">
        <el-radio-button value="week">周视图</el-radio-button>
        <el-radio-button value="list">列表视图</el-radio-button>
      </el-radio-group>

      <template v-if="view === 'week'">
        <el-button-group>
          <el-button icon="ArrowLeft" @click="shiftWeek(-1)">上一周</el-button>
          <el-button @click="resetWeek">本周</el-button>
          <el-button @click="shiftWeek(1)">下一周<el-icon class="el-icon--right"><ArrowRight /></el-icon></el-button>
        </el-button-group>
        <span class="range">
          {{ weekRange }}
          <el-tag v-if="isCurrentWeek" size="small" type="success" effect="plain">本周</el-tag>
          <el-tag size="small" type="info" effect="plain">已排 {{ weekData.length }} / 21 餐</el-tag>
        </span>
        <div class="right">
          <el-button type="primary" plain icon="CopyDocument" @click="handleCopyLast" v-hasPermi="['kids:menu:copy']">复制上周</el-button>
          <el-button icon="Printer" @click="printWeek">打印</el-button>
        </div>
      </template>
    </div>

    <!-- 周视图 -->
    <div v-show="view === 'week'" class="week-view" v-loading="weekLoading">
      <table class="week-table">
        <thead>
          <tr>
            <th class="meal-th">餐次</th>
            <th v-for="d in days" :key="d.date" :class="{ today: d.today }">
              <div class="day-label">{{ d.label }}</div>
              <div class="day-md">{{ d.md }}</div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in mealTypes" :key="m">
            <td class="meal-td">
              <el-tag :type="mealTag(m)" effect="light" size="small">{{ m }}</el-tag>
            </td>
            <td
              v-for="d in days"
              :key="d.date + m"
              :class="{ today: d.today, clickable: true }"
              @click="openCell(d.date, m)"
            >
              <div v-if="cell(m, d.date)" class="cell">
                <div class="dishes">{{ cell(m, d.date).dishes }}</div>
                <div v-if="cell(m, d.date).note" class="note">{{ cell(m, d.date).note }}</div>
              </div>
              <span v-else class="empty">+ 点击排餐</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 列表视图 -->
    <div v-show="view === 'list'">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="70px">
        <el-form-item label="菜品" prop="dishes">
          <el-input v-model="queryParams.dishes" placeholder="请输入菜品关键字" clearable style="width: 180px" @keyup.enter="getList" />
        </el-form-item>
        <el-form-item label="餐次" prop="mealType">
          <el-select v-model="queryParams.mealType" placeholder="全部餐次" clearable style="width: 130px">
            <el-option v-for="dict in meal_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="menuDate">
          <el-date-picker v-model="queryParams.menuDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:menu:add']">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:menu:remove']">删除</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="menuList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="日期" align="center" prop="menuDate" width="120" />
        <el-table-column label="餐次" align="center" width="100">
          <template #default="scope">
            <dict-tag :options="meal_type" :value="scope.row.mealType" />
          </template>
        </el-table-column>
        <el-table-column label="菜品" align="left" prop="dishes" :show-overflow-tooltip="true" min-width="260" />
        <el-table-column label="营养说明" align="left" prop="note" :show-overflow-tooltip="true" min-width="200" />
        <el-table-column label="发布人" align="center" prop="publisher" width="110" />
        <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:menu:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:menu:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="menuRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="日期" prop="menuDate">
              <el-date-picker v-model="form.menuDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="餐次" prop="mealType">
              <el-select v-model="form.mealType" placeholder="请选择餐次" style="width: 100%">
                <el-option v-for="dict in meal_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜品" prop="dishes">
              <el-input v-model="form.dishes" type="textarea" :rows="2" placeholder="如：小米粥、鸡蛋、牛奶" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="营养说明" prop="note">
              <el-input v-model="form.note" type="textarea" :rows="2" placeholder="如：补充蛋白质与钙" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布人" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入发布人" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button v-if="form.id" type="danger" plain @click="removeCurrent">删 除</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="KidsMenu">
import { listMenu, getMenu, addMenu, updateMenu, delMenu, menuWeek, copyLastWeek } from '@/api/kids/menu'
import { listAllergyChild } from '@/api/kids/child'

const { proxy } = getCurrentInstance()
const { meal_type } = useDict('meal_type')

const mealTypes = ['早餐', '午餐', '午点']
const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const pad = (n) => String(n).padStart(2, '0')
const fmt = (d) => d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
const parse = (s) => {
  const parts = s.split('-').map(Number)
  return new Date(parts[0], parts[1] - 1, parts[2])
}

/** 取某天所在周的周一 */
function mondayOf(date) {
  const d = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const wd = d.getDay() || 7
  d.setDate(d.getDate() - (wd - 1))
  return d
}

const today = fmt(new Date())
const weekStart = ref(fmt(mondayOf(new Date())))
const weekData = ref([])
const weekLoading = ref(false)
const view = ref('week')
const allergyList = ref([])

const days = computed(() =>
  dayLabels.map((label, i) => {
    const d = parse(weekStart.value)
    d.setDate(d.getDate() + i)
    const date = fmt(d)
    return { date, label, md: pad(d.getMonth() + 1) + '-' + pad(d.getDate()), today: date === today }
  })
)

const weekRange = computed(() => {
  const a = days.value[0]
  const b = days.value[6]
  return a.date + ' ~ ' + b.date
})

const isCurrentWeek = computed(() => weekStart.value === fmt(mondayOf(new Date())))

const weekMap = computed(() => {
  const map = {}
  weekData.value.forEach((p) => {
    if (!map[p.menuDate]) map[p.menuDate] = {}
    map[p.menuDate][p.mealType] = p
  })
  return map
})

function cell(meal, date) {
  return (weekMap.value[date] || {})[meal]
}

function mealTag(m) {
  return { 早餐: 'success', 午餐: 'warning', 午点: 'info' }[m] || 'info'
}

function loadWeek() {
  weekLoading.value = true
  menuWeek(weekStart.value).then((res) => {
    weekData.value = res.data || []
    weekLoading.value = false
  }).catch(() => {
    weekLoading.value = false
  })
}

function shiftWeek(n) {
  const d = parse(weekStart.value)
  d.setDate(d.getDate() + n * 7)
  weekStart.value = fmt(d)
}

function resetWeek() {
  weekStart.value = fmt(mondayOf(new Date()))
}

function handleCopyLast() {
  proxy.$modal.confirm('将上一周同星期几的食谱复制到本周，本周已排餐的日期会跳过、不覆盖。是否继续？').then(() => {
    return copyLastWeek(weekStart.value)
  }).then((res) => {
    const n = res.data || 0
    proxy.$modal.msgSuccess(n > 0 ? '已复制 ' + n + ' 条食谱' : '本周餐次已排满，没有可复制的记录')
    loadWeek()
    getList()
  }).catch(() => {})
}

function printWeek() {
  window.print()
}

watch(weekStart, loadWeek)

// ---------------- 列表与表单 ----------------
const menuList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, dishes: null, mealType: null, menuDate: null },
  rules: {
    menuDate: [{ required: true, message: '日期不能为空', trigger: 'change' }],
    mealType: [{ required: true, message: '请选择餐次', trigger: 'change' }],
    dishes: [{ required: true, message: '菜品不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMenu(queryParams.value).then((res) => {
    menuList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, menuDate: today, mealType: '午餐', dishes: null, note: null, publisher: null }
  proxy.resetForm('menuRef')
}

function resetQuery() {
  proxy.resetForm('queryRef')
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id)
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增食谱'
}

function handleUpdate(row) {
  reset()
  getMenu(row.id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改食谱'
  })
}

/** 周视图点格子：有记录就改，没有就按日期+餐次新建 */
function openCell(date, meal) {
  const existing = cell(meal, date)
  if (existing) {
    handleUpdate(existing)
  } else {
    reset()
    form.value.menuDate = date
    form.value.mealType = meal
    open.value = true
    title.value = '新增食谱'
  }
}

function submitForm() {
  proxy.$refs['menuRef'].validate((valid) => {
    if (!valid) return
    const action = form.value.id != null ? updateMenu(form.value) : addMenu(form.value)
    action.then(() => {
      proxy.$modal.msgSuccess(form.value.id != null ? '修改成功' : '新增成功')
      open.value = false
      loadWeek()
      getList()
    })
  })
}

function removeCurrent() {
  const id = form.value.id
  proxy.$modal.confirm('确定删除该条食谱吗？').then(() => {
    return delMenu(id)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    open.value = false
    loadWeek()
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的食谱？').then(() => {
    return delMenu(delIds)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    loadWeek()
    getList()
  }).catch(() => {})
}

onMounted(() => {
  loadWeek()
  getList()
  listAllergyChild().then((res) => {
    allergyList.value = res.data || []
  }).catch(() => {
    allergyList.value = []
  })
})
</script>

<style scoped>
.view-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.view-bar .range {
  color: #606266;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-bar .right {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.week-view {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.06);
  overflow-x: auto;
}

.week-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  min-width: 900px;
}

.week-table th,
.week-table td {
  border: 1px solid #ebeef5;
  padding: 8px 10px;
  vertical-align: top;
  text-align: left;
  font-size: 13px;
  color: #303133;
}

.week-table thead th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  text-align: center;
  padding: 8px 4px;
}

.week-table thead th.today {
  background: #ecf5ff;
  color: #409eff;
}

.week-table th.meal-th {
  width: 78px;
}

.week-table td.meal-td {
  text-align: center;
  vertical-align: middle;
  background: #fafafa;
}

.week-table td.today {
  background: #f5faff;
}

.week-table td.clickable {
  cursor: pointer;
}

.week-table td.clickable:hover {
  background: #f0f7ff;
}

.day-label {
  font-size: 13px;
}

.day-md {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-top: 2px;
}

.week-table tbody td {
  height: 96px;
}

.cell .dishes {
  line-height: 1.5;
  color: #303133;
}

.cell .note {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.empty {
  color: #c0c4cc;
  font-size: 12px;
}

@media print {
  .view-bar,
  :global(.aside),
  :global(.navbar),
  :global(.tags-view-container) {
    display: none !important;
  }

  .week-view {
    box-shadow: none;
    overflow: visible;
  }

  .week-table {
    min-width: 0;
  }
}
</style>
