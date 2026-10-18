<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb8">
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">食材品类</div>
          <div class="mini-value">{{ overview.kinds || 0 }}<span class="unit">种</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card" :class="{ danger: overview.warnCount > 0 }">
          <div class="mini-label">低于预警线</div>
          <div class="mini-value">{{ overview.warnCount || 0 }}<span class="unit">种</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">本月入库</div>
          <div class="mini-value">{{ overview.monthInTimes || 0 }}<span class="unit">笔</span></div>
          <div class="mini-sub">合计 {{ fmtQty(overview.monthInQty) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="mini-card">
          <div class="mini-label">本月出库</div>
          <div class="mini-value">{{ overview.monthOutTimes || 0 }}<span class="unit">笔</span></div>
          <div class="mini-sub">合计 {{ fmtQty(overview.monthOutQty) }}</div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="70px">
      <el-form-item label="食材" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入食材名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="全部分类" clearable style="width: 140px">
          <el-option v-for="dict in food_category" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警">
        <el-select v-model="queryParams.warnOnly" placeholder="全部" clearable style="width: 140px">
          <el-option label="仅看预警项" :value="true" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:food:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:food:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:food:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Download" @click="openStock('IN')" v-hasPermi="['kids:food:stock']">入库</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Upload" @click="openStock('OUT')" v-hasPermi="['kids:food:stock']">出库</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button icon="Tickets" @click="openRecords()">出入库流水</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="foodList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="食材名称" align="center" prop="name" width="140" />
      <el-table-column label="分类" align="center" width="110">
        <template #default="scope">
          <dict-tag :options="food_category" :value="scope.row.category" />
        </template>
      </el-table-column>
      <el-table-column label="库存数量" align="center" width="140">
        <template #default="scope">
          <span :class="{ warn: scope.row.warning }">{{ fmtQty(scope.row.quantity) }} {{ scope.row.unit }}</span>
          <el-tag v-if="scope.row.warning" type="danger" size="small" effect="plain" style="margin-left: 6px">预警</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预警线" align="center" width="120">
        <template #default="scope">{{ fmtQty(scope.row.warnLine) }} {{ scope.row.unit }}</template>
      </el-table-column>
      <el-table-column label="供应商" align="center" prop="supplier" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="success" icon="Download" @click="openStock('IN', scope.row)" v-hasPermi="['kids:food:stock']">入库</el-button>
          <el-button link type="warning" icon="Upload" @click="openStock('OUT', scope.row)" v-hasPermi="['kids:food:stock']">出库</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:food:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:food:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改食材 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="foodRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="食材名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入食材名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="dict in food_category" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="初始库存" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0" :precision="2" :step="1" :disabled="form.id != null" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如 kg / L / 个" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警线" prop="warnLine">
              <el-input-number v-model="form.warnLine" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplier">
              <el-input v-model="form.supplier" placeholder="请输入供应商" />
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="form.id != null" style="color: #909399; font-size: 12px; padding-left: 90px">
          库存数量请通过「入库 / 出库」调整，以便留下流水记录
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 出入库 -->
    <el-dialog v-model="stockVisible" :title="stockType === 'IN' ? '食材入库' : '食材出库'" width="520px" append-to-body destroy-on-close>
      <el-form :model="stockForm" label-width="90px">
        <el-form-item label="食材" required>
          <el-select v-model="stockForm.foodId" filterable placeholder="请选择食材" style="width: 100%">
            <el-option v-for="f in foodList" :key="f.id" :value="f.id" :label="f.name + '（' + fmtQty(f.quantity) + f.unit + '）'" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前库存">
          <span v-if="currentFood">
            {{ fmtQty(currentFood.quantity) }} {{ currentFood.unit }}
            <el-tag v-if="currentFood.warning" type="danger" size="small" effect="plain" style="margin-left: 6px">低于预警线</el-tag>
          </span>
          <span v-else style="color: #909399">请先选择食材</span>
        </el-form-item>
        <el-form-item :label="stockType === 'IN' ? '入库数量' : '出库数量'" required>
          <el-input-number v-model="stockForm.quantity" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作后">
          <span :style="{ color: afterValue < 0 ? '#f56c6c' : '#303133', fontWeight: 600 }">{{ afterText }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" :placeholder="stockType === 'IN' ? '如：采购入库 / 供应商送货' : '如：厨房领用 / 午餐制作'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="stockSaving" @click="submitStock">确 定</el-button>
          <el-button @click="stockVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 出入库流水 -->
    <el-dialog v-model="recordVisible" title="出入库流水" width="920px" append-to-body destroy-on-close>
      <el-form :inline="true" class="mb8">
        <el-form-item label="类型">
          <el-select v-model="recordQuery.type" placeholder="全部类型" clearable style="width: 130px" @change="loadRecords">
            <el-option label="入库" value="IN" />
            <el-option label="出库" value="OUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="食材">
          <el-select v-model="recordQuery.foodId" placeholder="全部食材" clearable filterable style="width: 180px" @change="loadRecords">
            <el-option v-for="f in foodList" :key="f.id" :value="f.id" :label="f.name" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button icon="Refresh" @click="loadRecords">刷新</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="recordLoading" :data="records" max-height="400">
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column prop="foodName" label="食材" width="110" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'IN' ? 'success' : 'warning'" size="small">{{ scope.row.type === 'IN' ? '入库' : '出库' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120" align="right">
          <template #default="scope">
            <span :style="{ color: scope.row.type === 'IN' ? '#67c23a' : '#e6a23c' }">
              {{ scope.row.type === 'IN' ? '+' : '-' }}{{ fmtQty(scope.row.quantity) }}{{ scope.row.unit }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="库存变化" min-width="160" align="center">
          <template #default="scope">{{ fmtQty(scope.row.beforeQty) }} → {{ fmtQty(scope.row.afterQty) }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>
      <pagination v-show="recordTotal > 0" :total="recordTotal" v-model:page="recordQuery.pageNum" v-model:limit="recordQuery.pageSize" @pagination="loadRecords" />
    </el-dialog>
  </div>
</template>

<script setup name="KidsFood">
import { listFood, getFood, addFood, updateFood, delFood, foodOverview, changeStock, listStockRecord } from '@/api/kids/food'

const { proxy } = getCurrentInstance()
const { food_category } = useDict('food_category')

const foodList = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, name: null, category: null, warnOnly: null },
  rules: {
    name: [{ required: true, message: '食材名称不能为空', trigger: 'blur' }],
    unit: [{ required: true, message: '计量单位不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function fmtQty(v) {
  const n = Number(v || 0)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

function getList() {
  loading.value = true
  listFood(queryParams.value).then((res) => {
    foodList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function loadOverview() {
  foodOverview().then((res) => {
    overview.value = res.data || {}
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, name: null, category: '主食', quantity: 0, unit: 'kg', warnLine: 5, supplier: null }
  proxy.resetForm('foodRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.warnOnly = null
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
  title.value = '新增食材'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getFood(id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改食材'
  })
}

function submitForm() {
  proxy.$refs['foodRef'].validate((valid) => {
    if (!valid) return
    const action = form.value.id != null ? updateFood(form.value) : addFood(form.value)
    action.then(() => {
      proxy.$modal.msgSuccess(form.value.id != null ? '修改成功' : '新增成功')
      open.value = false
      getList()
      loadOverview()
    })
  })
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的食材？').then(() => {
    return delFood(delIds)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
    loadOverview()
  }).catch(() => {})
}

// ---------------- 出入库 ----------------
const stockVisible = ref(false)
const stockSaving = ref(false)
const stockType = ref('IN')
const stockForm = reactive({ foodId: null, quantity: 1, remark: '' })

const currentFood = computed(() => foodList.value.find((f) => f.id === stockForm.foodId) || null)

const afterValue = computed(() => {
  if (!currentFood.value) return 0
  const base = Number(currentFood.value.quantity || 0)
  const n = Number(stockForm.quantity || 0)
  return stockType.value === 'IN' ? base + n : base - n
})

const afterText = computed(() => {
  if (!currentFood.value) return '—'
  return fmtQty(afterValue.value) + ' ' + (currentFood.value.unit || '')
})

function openStock(type, row) {
  stockType.value = type
  stockForm.foodId = row ? row.id : null
  stockForm.quantity = 1
  stockForm.remark = ''
  if (!row && foodList.value.length === 0) {
    proxy.$modal.msgWarning('请先新增食材')
    return
  }
  stockVisible.value = true
}

function submitStock() {
  if (!stockForm.foodId) {
    proxy.$modal.msgWarning('请选择食材')
    return
  }
  if (Number(stockForm.quantity) <= 0) {
    proxy.$modal.msgWarning('数量必须大于 0')
    return
  }
  stockSaving.value = true
  changeStock(stockForm.foodId, {
    type: stockType.value,
    quantity: stockForm.quantity,
    remark: stockForm.remark
  }).then(() => {
    proxy.$modal.msgSuccess(stockType.value === 'IN' ? '入库成功' : '出库成功')
    stockSaving.value = false
    stockVisible.value = false
    getList()
    loadOverview()
  }).catch(() => {
    stockSaving.value = false
  })
}

// ---------------- 流水 ----------------
const recordVisible = ref(false)
const recordLoading = ref(false)
const records = ref([])
const recordTotal = ref(0)
const recordQuery = reactive({ pageNum: 1, pageSize: 10, type: null, foodId: null })

function openRecords(foodId) {
  recordQuery.pageNum = 1
  recordQuery.foodId = foodId || null
  recordVisible.value = true
  loadRecords()
}

function loadRecords() {
  recordLoading.value = true
  listStockRecord(recordQuery).then((res) => {
    records.value = res.rows || []
    recordTotal.value = res.total || 0
    recordLoading.value = false
  }).catch(() => {
    recordLoading.value = false
  })
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

.mini-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.warn {
  color: #f56c6c;
  font-weight: 600;
}
</style>
