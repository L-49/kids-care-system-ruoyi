<template>
  <div class="app-container report">
    <!-- 概览指标 -->
    <el-row :gutter="12" class="mb8">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="mini-card">
          <div class="mini-label">在园幼儿</div>
          <div class="mini-value">{{ data.childCount || 0 }}<span class="unit">人</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="mini-card">
          <div class="mini-label">本月实收</div>
          <div class="mini-value">{{ money(data.monthIncome) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="mini-card">
          <div class="mini-label">待缴金额</div>
          <div class="mini-value danger">{{ money(data.unpaidAmount) }}</div>
          <div class="mini-sub">{{ data.unpaidCount || 0 }} 笔未结清</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="mini-card">
          <div class="mini-label">今日考勤登记</div>
          <div class="mini-value">{{ data.todayTotal || 0 }}<span class="unit">人次</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="mini-card" :class="{ danger: data.stockWarn > 0 }">
          <div class="mini-label">食材预警</div>
          <div class="mini-value">{{ data.stockWarn || 0 }}<span class="unit">项</span></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="mini-card">
          <div class="mini-label">操作日志</div>
          <div class="mini-value">{{ data.logCount || 0 }}<span class="unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="12" class="mb8">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="box-card">
          <template #header>
            <div class="card-head">
              <span>近 6 个月收费情况</span>
              <el-button icon="Refresh" link type="primary" @click="load">刷新</el-button>
            </div>
          </template>
          <div ref="feeRef" v-loading="loading" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="box-card">
          <template #header>
            <div class="card-head"><span>今日考勤构成</span></div>
          </template>
          <div ref="attRef" v-loading="loading" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :xs="24" :md="9">
        <el-card shadow="never" class="box-card">
          <template #header>
            <div class="card-head"><span>各班出勤率</span></div>
          </template>
          <div ref="clazzRef" v-loading="loading" class="chart tall"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="15">
        <el-card shadow="never" class="box-card">
          <template #header>
            <div class="card-head">
              <span>班级出勤明细</span>
              <span class="card-tip">累计统计，含历史记录</span>
            </div>
          </template>
          <el-table v-loading="loading" :data="clazzRows" size="small" max-height="360">
            <el-table-column prop="clazzName" label="班级" width="120" />
            <el-table-column prop="present" label="出勤人次" width="100" align="center" />
            <el-table-column prop="total" label="应到人次" width="100" align="center" />
            <el-table-column label="出勤率" min-width="180">
              <template #default="scope">
                <el-progress
                  :percentage="scope.row.rate"
                  :stroke-width="12"
                  :color="rateColor(scope.row.rate)"
                  :format="() => scope.row.rate + '%'"
                />
              </template>
            </el-table-column>
            <el-table-column label="水平" width="90" align="center">
              <template #default="scope">
                <el-tag :type="rateTag(scope.row.rate)" size="small" effect="light">{{ rateText(scope.row.rate) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-foot">
            合计：出勤 {{ totalPresent }} 人次 / 应到 {{ totalShould }} 人次，整体出勤率
            <b>{{ overallRate }}%</b>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="KidsReport">
import * as echarts from 'echarts'
import { statReport } from '@/api/kids/stat'

const loading = ref(true)
const data = ref({})
const feeRef = ref(null)
const attRef = ref(null)
const clazzRef = ref(null)
let feeChart = null
let attChart = null
let clazzChart = null

function money(v) {
  const n = Number(v || 0)
  return '¥' + n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const clazzRows = computed(() => {
  return (data.value.attendanceByClazz || []).map((r) => {
    const total = Number(r.total || 0)
    const present = Number(r.present || 0)
    return {
      clazzName: r.clazzName,
      present,
      total,
      rate: total ? Number(((present / total) * 100).toFixed(1)) : 0
    }
  })
})

const totalPresent = computed(() => clazzRows.value.reduce((s, r) => s + r.present, 0))
const totalShould = computed(() => clazzRows.value.reduce((s, r) => s + r.total, 0))
const overallRate = computed(() => {
  const t = totalShould.value
  return t ? ((totalPresent.value / t) * 100).toFixed(1) : '0.0'
})

function rateColor(rate) {
  if (rate >= 90) return '#67c23a'
  if (rate >= 75) return '#e6a23c'
  return '#f56c6c'
}

function rateTag(rate) {
  if (rate >= 90) return 'success'
  if (rate >= 75) return 'warning'
  return 'danger'
}

function rateText(rate) {
  if (rate >= 90) return '良好'
  if (rate >= 75) return '正常'
  return '偏低'
}

function renderFee(rows) {
  if (!feeChart) return
  // 后端按月份倒序返回，图表需要正序
  const list = (rows || []).slice().reverse()
  feeChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['实收金额', '应收金额'], top: 0, right: 10 },
    grid: { left: 60, right: 24, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: list.map((r) => r.month) },
    yAxis: { type: 'value', name: '金额（元）' },
    series: [
      {
        name: '实收金额',
        type: 'bar',
        barWidth: 20,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
        data: list.map((r) => Number(r.paid || 0))
      },
      {
        name: '应收金额',
        type: 'line',
        smooth: true,
        symbolSize: 7,
        itemStyle: { color: '#e6a23c' },
        data: list.map((r) => Number(r.total || 0))
      }
    ]
  })
}

function renderAtt(rows) {
  if (!attChart) return
  const order = ['出勤', '迟到', '请假', '缺勤']
  const colors = { 出勤: '#67c23a', 迟到: '#e6a23c', 请假: '#909399', 缺勤: '#f56c6c' }
  const map = {}
  ;(rows || []).forEach((r) => {
    map[r.status] = Number(r.num || 0)
  })
  const list = order.filter((k) => map[k] != null).map((k) => ({ name: k, value: map[k] }))
  attChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}：{c} 人（{d}%）' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
    series: [
      {
        type: 'pie',
        radius: ['38%', '62%'],
        center: ['50%', '46%'],
        label: { formatter: '{b}\n{c} 人' },
        data: list.map((i) => ({ ...i, itemStyle: { color: colors[i.name] } }))
      }
    ]
  })
}

function renderClazz(rows) {
  if (!clazzChart) return
  const list = clazzRows.value.slice().reverse()
  clazzChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}：{c}%' },
    grid: { left: 70, right: 40, top: 10, bottom: 20 },
    xAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: list.map((r) => r.clazzName) },
    series: [
      {
        type: 'bar',
        barWidth: 14,
        label: { show: true, position: 'right', formatter: '{c}%' },
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: (p) => (p.value >= 90 ? '#67c23a' : p.value >= 75 ? '#e6a23c' : '#f56c6c')
        },
        data: list.map((r) => r.rate)
      }
    ]
  })
}

function load() {
  loading.value = true
  statReport().then((res) => {
    data.value = res.data || {}
    loading.value = false
    nextTick(() => {
      renderFee(data.value.feeTrend)
      renderAtt(data.value.attendanceToday)
      renderClazz()
    })
  }).catch(() => {
    loading.value = false
  })
}

function resize() {
  feeChart && feeChart.resize()
  attChart && attChart.resize()
  clazzChart && clazzChart.resize()
}

onMounted(() => {
  feeChart = echarts.init(feeRef.value)
  attChart = echarts.init(attRef.value)
  clazzChart = echarts.init(clazzRef.value)
  load()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  feeChart && feeChart.dispose()
  attChart && attChart.dispose()
  clazzChart && clazzChart.dispose()
})
</script>

<style scoped>
.report {
  background: #f3f5f9;
}

.mini-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 18px;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.06);
  border-left: 3px solid #409eff;
  margin-bottom: 4px;
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
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.mini-value.danger {
  color: #f56c6c;
}

.mini-value .unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 3px;
}

.mini-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.box-card {
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
  height: 280px;
  width: 100%;
}

.chart.tall {
  height: 360px;
}

.table-foot {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
  font-size: 12px;
  color: #606266;
  text-align: right;
}
</style>
