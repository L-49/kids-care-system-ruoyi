<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb8">
      <el-col :xs="12" :md="6">
        <div class="fee-card">
          <div class="label">本月应收（元）</div>
          <div class="value">{{ summary.total }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="fee-card">
          <div class="label">本月已收（元）</div>
          <div class="value paid">{{ summary.paid }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="fee-card">
          <div class="label">待收金额（元）</div>
          <div class="value unpaid">{{ summary.unpaid }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="fee-card">
          <div class="label">未缴笔数</div>
          <div class="value">{{ summary.unpaidCount }} 笔</div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="收费月份" prop="feeMonth">
        <el-date-picker v-model="queryParams.feeMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width: 160px" />
      </el-form-item>
      <el-form-item label="费用类型" prop="feeType">
        <el-select v-model="queryParams.feeType" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="dict in fee_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="缴费状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="未缴费" :value="0" />
          <el-option label="已缴费" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['kids:fee:add']">新增账单</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['kids:fee:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['kids:fee:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="feeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="幼儿" align="center" prop="childName" width="110" />
      <el-table-column label="费用类型" align="center" width="110">
        <template #default="scope">
          <dict-tag :options="fee_type" :value="scope.row.feeType" />
        </template>
      </el-table-column>
      <el-table-column label="收费月份" align="center" prop="feeMonth" width="110" />
      <el-table-column label="金额" align="center" width="120">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: 600">¥{{ Number(scope.row.amount || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="缴费状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'warning'">{{ scope.row.payStatus === 1 ? '已缴费' : '未缴费' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="缴费时间" align="center" prop="payTime" width="170" />
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-if="scope.row.payStatus === 0" link type="success" icon="Money" @click="handlePay(scope.row)" v-hasPermi="['kids:fee:pay']">确认收款</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kids:fee:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kids:fee:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="feeRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="幼儿" prop="childId">
          <el-select v-model="form.childId" placeholder="请选择幼儿" filterable style="width: 100%">
            <el-option v-for="item in childList" :key="item.id" :label="item.name + '（' + (item.clazzName || '未分班') + '）'" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型" prop="feeType">
          <el-select v-model="form.feeType" placeholder="请选择费用类型" style="width: 100%">
            <el-option v-for="dict in fee_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="收费月份" prop="feeMonth">
          <el-date-picker v-model="form.feeMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width: 100%" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="100" style="width: 200px" />
          <span style="margin-left: 8px">元</span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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

<script setup name="KidsFee">
import { listFee, getFee, addFee, updateFee, delFee, payFee } from '@/api/kids/fee'
import { listChildAll } from '@/api/kids/child'

const { proxy } = getCurrentInstance()
const { fee_type } = useDict('fee_type')

const feeList = ref([])
const childList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const summary = ref({ total: '0.00', paid: '0.00', unpaid: '0.00', unpaidCount: 0 })

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, feeMonth: null, feeType: null, payStatus: null },
  rules: {
    childId: [{ required: true, message: '请选择幼儿', trigger: 'change' }],
    feeType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
    feeMonth: [{ required: true, message: '请选择收费月份', trigger: 'change' }],
    amount: [{ required: true, message: '金额不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listFee(queryParams.value).then((res) => {
    feeList.value = res.rows
    total.value = res.total
    loading.value = false
    calcSummary()
  })
}

/** 汇总当前筛选结果，不额外请求接口 */
function calcSummary() {
  let all = 0
  let paid = 0
  let unpaid = 0
  let unpaidCount = 0
  feeList.value.forEach((row) => {
    const amount = Number(row.amount || 0)
    all += amount
    if (row.payStatus === 1) {
      paid += amount
    } else {
      unpaid += amount
      unpaidCount++
    }
  })
  summary.value = { total: all.toFixed(2), paid: paid.toFixed(2), unpaid: unpaid.toFixed(2), unpaidCount }
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = { id: null, childId: null, feeType: '保教费', feeMonth: null, amount: null, remark: null }
  proxy.resetForm('feeRef')
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
  title.value = '新增账单'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getFee(id).then((res) => {
    form.value = res.data
    open.value = true
    title.value = '修改账单'
  })
}

function submitForm() {
  proxy.$refs['feeRef'].validate((valid) => {
    if (!valid) return
    if (form.value.id != null) {
      updateFee(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addFee(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handlePay(row) {
  proxy.$modal.confirm('确认收到该笔费用 ¥' + Number(row.amount || 0).toFixed(2) + ' ？').then(() => {
    return payFee(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('收款成功')
  }).catch(() => {})
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的账单？').then(() => {
    return delFee(delIds)
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

<style scoped>
.fee-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 18px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  margin-bottom: 12px;
}

.fee-card .label {
  color: #909399;
  font-size: 13px;
}

.fee-card .value {
  font-size: 22px;
  font-weight: 600;
  margin-top: 6px;
  color: #303133;
}

.fee-card .paid {
  color: #67c23a;
}

.fee-card .unpaid {
  color: #f56c6c;
}
</style>
