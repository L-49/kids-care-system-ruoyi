<template>
  <div class="app-container dashboard">
    <!-- 顶部四个核心指标 -->
    <el-row :gutter="12" class="mb8">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card blue">
          <div class="stat-head">
            <span class="stat-label">在园幼儿</span>
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-value">{{ ov.childCount || 0 }}<span class="unit">人</span></div>
          <div class="stat-sub">共 {{ ov.clazzCount || 0 }} 个班级</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card green">
          <div class="stat-head">
            <span class="stat-label">在职教职工</span>
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="stat-value">{{ ov.teacherCount || 0 }}<span class="unit">人</span></div>
          <div class="stat-sub">今日出勤 {{ todayPresent }} 人</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card orange" :class="{ clickable: isStaff }" @click="goLeave">
          <div class="stat-head">
            <span class="stat-label">待审批请假</span>
            <el-icon><Message /></el-icon>
          </div>
          <div class="stat-value">{{ ov.pendingLeave || 0 }}<span class="unit">条</span></div>
          <div class="stat-sub">{{ isStaff ? '点击前往处理' : '审批进度可在请假记录查看' }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card red" :class="{ clickable: isStaff }" @click="goFee">
          <div class="stat-head">
            <span class="stat-label">待缴费用</span>
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-value">{{ ov.unpaidCount || 0 }}<span class="unit">笔</span></div>
          <div class="stat-sub">合计 {{ money(ov.unpaidAmount) }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="12" class="mb8">
      <el-col :xs="24" :md="15">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head">
              <span>近 7 天出勤趋势</span>
              <span class="card-tip">仅统计工作日</span>
            </div>
          </template>
          <div ref="trendRef" v-loading="loading" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="9">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head"><span>班级人数分布</span></div>
          </template>
          <div ref="pieRef" v-loading="loading" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <!-- 今日考勤概况 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head"><span>今日考勤概况</span></div>
          </template>
          <div v-loading="loading" class="att-box">
            <div v-for="item in attendanceItems" :key="item.label" class="att-row">
              <div class="att-name">
                <el-tag :type="item.type" size="small" effect="light">{{ item.label }}</el-tag>
              </div>
              <div class="att-bar">
                <div class="att-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
              <div class="att-num">{{ item.value }} 人</div>
            </div>
            <div class="att-foot">
              今日已登记 <b>{{ todayTotal }}</b> 人次，应到 <b>{{ ov.childCount || 0 }}</b> 人
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 待缴费提醒 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head">
              <span>待缴费提醒</span>
              <el-link v-if="isStaff" type="primary" underline="never" @click="goFee">查看全部</el-link>
            </div>
          </template>
          <el-table v-loading="loading" :data="ov.unpaidFeeTop || []" size="small" empty-text="暂无待缴费用">
            <el-table-column prop="childName" label="幼儿" width="100" />
            <el-table-column prop="feeType" label="费用类型" width="110" />
            <el-table-column prop="feeMonth" label="所属月份" width="110" />
            <el-table-column label="金额" align="right">
              <template #default="scope">
                <span style="color: #f56c6c; font-weight: 600">{{ money(scope.row.amount) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!isStaff" class="parent-tip">
            家长的账单明细请在「我的孩子」页面查看
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="KidsDashboard">
import * as echarts from 'echarts'
import { statOverview } from '@/api/kids/stat'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const loading = ref(true)
const ov = ref({})
const trendRef = ref(null)
const pieRef = ref(null)
let trendChart = null
let pieChart = null

/** 园长 / 教师才有的业务入口，家长只读 */
const isStaff = computed(() => {
  const roles = userStore.roles || []
  return roles.includes('admin') || roles.includes('teacher')
})

const todayPresent = computed(() => Number((ov.value.attendanceToday || {})['出勤'] || 0))
const todayTotal = computed(() => Number(ov.value.attendanceTodayTotal || 0) || 0)

const ATT_META = [
  { key: '出勤', label: '出勤', type: 'success', color: '#67c23a' },
  { key: '迟到', label: '迟到', type: 'warning', color: '#e6a23c' },
  { key: '请假', label: '请假', type: 'info', color: '#909399' },
  { key: '缺勤', label: '缺勤', type: 'danger', color: '#f56c6c' }
]

const attendanceItems = computed(() => {
  const today = ov.value.attendanceToday || {}
  const max = Math.max(1, ...ATT_META.map((m) => Number(today[m.key] || 0)))
  return ATT_META.map((m) => {
    const value = Number(today[m.key] || 0)
    return { ...m, value, percent: Math.round((value / max) * 100) }
  })
})

function money(v) {
  const n = Number(v || 0)
  return '¥' + n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function goLeave() {
  if (!isStaff.value) return
  proxy.$router.push('/teach/leave')
}

function goFee() {
  if (!isStaff.value) return
  proxy.$router.push('/finance/fee')
}

function renderTrend(rows) {
  if (!trendChart) return
  const list = rows || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['出勤人数', '出勤率'], right: 10, top: 0 },
    grid: { left: 40, right: 50, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: list.map((r) => r.day), boundaryGap: true },
    yAxis: [
      { type: 'value', name: '人数', minInterval: 1 },
      { type: 'value', name: '出勤率', min: 0, max: 100, axisLabel: { formatter: '{value}%' } }
    ],
    series: [
      {
        name: '出勤人数',
        type: 'bar',
        barWidth: 22,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
        data: list.map((r) => Number(r.present || 0))
      },
      {
        name: '出勤率',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbolSize: 7,
        itemStyle: { color: '#67c23a' },
        data: list.map((r) => {
          const total = Number(r.total || 0)
          return total ? Number(((Number(r.present || 0) / total) * 100).toFixed(1)) : 0
        })
      }
    ]
  })
}

function renderPie(rows) {
  if (!pieChart) return
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}：{c} 人（{d}%）' },
    legend: { orient: 'vertical', right: 6, top: 'center', itemWidth: 10, itemHeight: 10 },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: true,
        label: { show: false },
        data: (rows || []).map((r) => ({ name: r.name, value: Number(r.value || 0) }))
      }
    ]
  })
}

function load() {
  loading.value = true
  statOverview().then((res) => {
    const d = res.data || {}
    ov.value = d
    // 今日登记总人次由四种状态相加得出，避免额外接口
    const today = d.attendanceToday || {}
    ov.value.attendanceTodayTotal = Object.values(today).reduce((s, v) => s + Number(v || 0), 0)
    loading.value = false
    nextTick(() => {
      renderTrend(d.attendanceTrend)
      renderPie(d.clazzDistribution)
    })
  }).catch(() => {
    loading.value = false
  })
}

function resize() {
  trendChart && trendChart.resize()
  pieChart && pieChart.resize()
}

onMounted(() => {
  trendChart = echarts.init(trendRef.value)
  pieChart = echarts.init(pieRef.value)
  load()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  trendChart && trendChart.dispose()
  pieChart && pieChart.dispose()
})
</script>

<style scoped>
.dashboard {
  background: #f3f5f9;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 18px;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.06);
  border-left: 3px solid #409eff;
  margin-bottom: 4px;
}

.stat-card.clickable {
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.stat-card.clickable:hover {
  box-shadow: 0 3px 14px rgba(0, 21, 41, 0.14);
}

.stat-card.green {
  border-left-color: #67c23a;
}

.stat-card.orange {
  border-left-color: #e6a23c;
}

.stat-card.red {
  border-left-color: #f56c6c;
}

.stat-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-value .unit {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
  margin-left: 4px;
}

.stat-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.chart-card {
  border: none;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.06);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.card-tip {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

.chart {
  height: 260px;
  width: 100%;
}

.att-box {
  min-height: 200px;
}

.att-row {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}

.att-name {
  width: 62px;
  flex: none;
}

.att-bar {
  flex: 1;
  height: 10px;
  background: #f0f2f5;
  border-radius: 6px;
  overflow: hidden;
  margin: 0 10px;
}

.att-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.4s;
}

.att-num {
  width: 56px;
  text-align: right;
  font-size: 13px;
  color: #606266;
}

.att-foot {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
  font-size: 12px;
  color: #909399;
}

.parent-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
