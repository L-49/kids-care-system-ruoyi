<template>
  <div class="app-container my-child" v-loading="loading">
    <template v-if="child.id">
      <!-- 孩子档案 -->
      <el-row :gutter="12" class="mb8">
        <el-col :xs="24" :md="8">
          <div class="profile-card">
            <div class="avatar">{{ (child.name || '?').slice(0, 1) }}</div>
            <div class="profile-main">
              <div class="profile-name">
                {{ child.name }}
                <el-tag :type="child.status === 1 ? 'success' : 'info'" size="small" effect="light">
                  {{ child.status === 1 ? '在园' : '离园' }}
                </el-tag>
              </div>
              <div class="profile-line">
                <span>{{ child.gender || '—' }}</span>
                <el-divider direction="vertical" />
                <span>{{ ageText }}</span>
                <el-divider direction="vertical" />
                <span>{{ clazz.name || '未分班' }}</span>
              </div>
              <div class="profile-line sub">
                入园日期：{{ child.enrollDate || '—' }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :md="16">
          <el-row :gutter="12">
            <el-col :xs="12" :sm="6">
              <div class="mini-card">
                <div class="mini-label">近期出勤率</div>
                <div class="mini-value" :style="{ color: attendRate >= 90 ? '#67c23a' : '#e6a23c' }">{{ attendRate }}%</div>
                <div class="mini-sub">近 {{ attendances.length }} 次记录</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="mini-card" :class="{ danger: unpaidTotal > 0 }">
                <div class="mini-label">待缴费用</div>
                <div class="mini-value">{{ money(unpaidTotal) }}</div>
                <div class="mini-sub">{{ unpaidFees.length }} 笔未结清</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="mini-card">
                <div class="mini-label">班主任</div>
                <div class="mini-value small">{{ clazz.headTeacherName || '待安排' }}</div>
                <div class="mini-sub">{{ clazz.room || '教室待定' }}</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="mini-card">
                <div class="mini-label">护理提示</div>
                <div class="mini-value small">{{ child.allergy ? '有过敏禁忌' : '无特殊禁忌' }}</div>
                <div class="mini-sub" :title="child.allergy">{{ child.allergy || '暂无记录' }}</div>
              </div>
            </el-col>
          </el-row>
        </el-col>
      </el-row>

      <!-- 今日状态 + 档案信息 -->
      <el-row :gutter="12" class="mb8">
        <el-col :xs="24" :md="8">
          <el-card shadow="never" class="box-card today-card">
            <template #header>
              <div class="card-head"><span>今日状态</span><span class="card-tip">{{ today }}</span></div>
            </template>
            <div class="today-body">
              <div class="today-status" :style="{ background: todayMeta.bg, color: todayMeta.color }">
                {{ todayMeta.label }}
              </div>
              <div class="today-line" v-if="todayRecord && todayRecord.checkInTime">
                入园时间：{{ todayRecord.checkInTime }}
              </div>
              <div class="today-line" v-else>暂无入园打卡记录</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="16">
          <el-card shadow="never" class="box-card">
            <template #header>
              <div class="card-head"><span>档案信息</span></div>
            </template>
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item label="姓名">{{ child.name || '—' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ child.gender || '—' }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ child.birthday || '—' }}</el-descriptions-item>
              <el-descriptions-item label="所在班级">{{ clazz.name || '—' }}</el-descriptions-item>
              <el-descriptions-item label="家长姓名">{{ child.guardianName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ child.guardianPhone || '—' }}</el-descriptions-item>
              <el-descriptions-item label="家庭住址" :span="2">{{ child.address || '—' }}</el-descriptions-item>
              <el-descriptions-item label="过敏史/禁忌" :span="2">
                <el-tag v-if="child.allergy" type="danger" size="small" effect="light">{{ child.allergy }}</el-tag>
                <span v-else>无</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <!-- 明细标签页 -->
      <el-card shadow="never" class="box-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="`近期考勤（${attendances.length}）`" name="attendance">
            <el-table :data="attendances" size="small" max-height="380" empty-text="暂无考勤记录">
              <el-table-column prop="attDate" label="日期" width="130" />
              <el-table-column label="状态" width="110" align="center">
                <template #default="scope">
                  <el-tag :type="attTag(scope.row.status)" size="small" effect="light">{{ scope.row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="入园时间" width="130">
                <template #default="scope">{{ scope.row.checkInTime || '—' }}</template>
              </el-table-column>
              <el-table-column label="说明">
                <template #default="scope">{{ attTip(scope.row.status) }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="`待缴账单（${unpaidFees.length}）`" name="fee">
            <el-table :data="unpaidFees" size="small" max-height="380" empty-text="太棒了，暂无待缴费用">
              <el-table-column prop="feeMonth" label="所属月份" width="130" />
              <el-table-column prop="feeType" label="费用类型" width="140" />
              <el-table-column label="金额" width="140" align="right">
                <template #default="scope">
                  <span style="color: #f56c6c; font-weight: 600">{{ money(scope.row.amount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="缴费方式">
                <template #default>
                  请前往园所财务处或通过班级教师代缴
                </template>
              </el-table-column>
            </el-table>
            <div class="table-foot" v-if="unpaidFees.length">
              合计待缴 <b style="color: #f56c6c">{{ money(unpaidTotal) }}</b>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`请假记录（${leaves.length}）`" name="leave">
            <div class="mb8">
              <el-button type="primary" plain icon="Plus" @click="openLeave">申请请假</el-button>
            </div>
            <el-table :data="leaves" size="small" max-height="360" empty-text="暂无请假记录">
              <el-table-column label="起止日期" width="230">
                <template #default="scope">{{ scope.row.startDate }} 至 {{ scope.row.endDate }}</template>
              </el-table-column>
              <el-table-column prop="days" label="天数" width="80" align="center" />
              <el-table-column prop="reason" label="事由" min-width="160" show-overflow-tooltip />
              <el-table-column label="状态" width="110" align="center">
                <template #default="scope">
                  <el-tag :type="leaveTag(scope.row.status)" size="small" effect="light">{{ leaveText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="审批人" width="110">
                <template #default="scope">{{ scope.row.approver || '—' }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="`园所公告（${notices.length}）`" name="notice">
            <div v-if="!notices.length" class="empty-text">暂无公告</div>
            <div v-for="n in notices" :key="n.id" class="notice-item">
              <div class="notice-head">
                <span class="notice-title">{{ n.title }}</span>
                <el-tag size="small" effect="plain" type="info">{{ n.type || '园务' }}</el-tag>
                <span class="notice-time">{{ n.publishTime || '' }}</span>
              </div>
              <div class="notice-body">{{ n.content }}</div>
              <div class="notice-foot">发布人：{{ n.publisher || '园务办公室' }}</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>

    <el-empty v-else-if="!loading" description="当前账号未绑定幼儿档案，请联系园方处理" />

    <!-- 请假申请 -->
    <el-dialog v-model="leaveOpen" title="申请请假" width="480px" append-to-body destroy-on-close>
      <el-form ref="leaveRef" :model="leaveForm" :rules="leaveRules" label-width="90px">
        <el-form-item label="幼儿">
          <el-input :model-value="child.name" disabled />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="leaveForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="leaveForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假天数">
          <span>{{ leaveDays }} 天</span>
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="leaveForm.reason" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请简要说明请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="leaveSaving" @click="submitLeave">提 交</el-button>
          <el-button @click="leaveOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="KidsMyChild">
import { myChildInfo, applyMyLeave } from '@/api/kids/myChild'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const child = ref({})
const clazz = ref({})
const attendances = ref([])
const unpaidFees = ref([])
const leaves = ref([])
const notices = ref([])
const activeTab = ref('attendance')

const today = (() => {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
})()

const unpaidTotal = computed(() => unpaidFees.value.reduce((s, f) => s + Number(f.amount || 0), 0))

const attendRate = computed(() => {
  const list = attendances.value
  if (!list.length) return 0
  const present = list.filter((a) => a.status === '出勤' || a.status === '迟到').length
  return Number(((present / list.length) * 100).toFixed(1))
})

const ageText = computed(() => {
  const b = child.value.birthday
  if (!b) return '年龄未知'
  const birth = new Date(b)
  if (isNaN(birth.getTime())) return '年龄未知'
  const now = new Date()
  let months = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth())
  if (now.getDate() < birth.getDate()) months -= 1
  if (months < 0) return '年龄未知'
  const y = Math.floor(months / 12)
  const m = months % 12
  return y > 0 ? `${y} 岁 ${m} 个月` : `${m} 个月`
})

const todayRecord = computed(() => attendances.value.find((a) => a.attDate === today) || null)

const todayMeta = computed(() => {
  const status = todayRecord.value ? todayRecord.value.status : ''
  switch (status) {
    case '出勤':
      return { label: '今日在园', color: '#67c23a', bg: '#f0f9eb' }
    case '迟到':
      return { label: '今日迟到', color: '#e6a23c', bg: '#fdf6ec' }
    case '请假':
      return { label: '今日请假', color: '#909399', bg: '#f4f4f5' }
    case '缺勤':
      return { label: '今日缺勤', color: '#f56c6c', bg: '#fef0f0' }
    default:
      return { label: '暂无记录', color: '#909399', bg: '#f4f4f5' }
  }
})

function money(v) {
  return '¥' + Number(v || 0).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function attTag(status) {
  if (status === '出勤') return 'success'
  if (status === '迟到') return 'warning'
  if (status === '请假') return 'info'
  return 'danger'
}

function attTip(status) {
  if (status === '出勤') return '正常入园'
  if (status === '迟到') return '晚于规定时间入园'
  if (status === '请假') return '已提交请假申请'
  if (status === '缺勤') return '未入园且未请假'
  return ''
}

function leaveTag(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function leaveText(status) {
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '待审批'
}

function load() {
  loading.value = true
  myChildInfo().then((res) => {
    const d = res.data || {}
    child.value = d.child || {}
    clazz.value = d.clazz || {}
    attendances.value = d.attendances || []
    unpaidFees.value = d.unpaidFees || []
    leaves.value = d.leaves || []
    notices.value = d.notices || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// ---------------- 请假申请 ----------------
const leaveOpen = ref(false)
const leaveSaving = ref(false)
const leaveForm = reactive({ startDate: null, endDate: null, reason: '' })
const leaveRules = {
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  reason: [{ required: true, message: '请填写请假事由', trigger: 'blur' }]
}

const leaveDays = computed(() => {
  if (!leaveForm.startDate || !leaveForm.endDate) return 0
  const s = new Date(leaveForm.startDate)
  const e = new Date(leaveForm.endDate)
  const diff = Math.floor((e - s) / 86400000) + 1
  return diff > 0 ? diff : 0
})

function openLeave() {
  leaveForm.startDate = null
  leaveForm.endDate = null
  leaveForm.reason = ''
  leaveOpen.value = true
}

function submitLeave() {
  proxy.$refs['leaveRef'].validate((valid) => {
    if (!valid) return
    if (new Date(leaveForm.endDate) < new Date(leaveForm.startDate)) {
      proxy.$modal.msgWarning('结束日期不能早于开始日期')
      return
    }
    leaveSaving.value = true
    applyMyLeave({ ...leaveForm }).then(() => {
      proxy.$modal.msgSuccess('请假申请已提交，请等待教师审批')
      leaveSaving.value = false
      leaveOpen.value = false
      load()
    }).catch(() => {
      leaveSaving.value = false
    })
  })
}

onMounted(load)
</script>

<style scoped>
.my-child {
  background: #f3f5f9;
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.06);
  display: flex;
  align-items: center;
  height: 100%;
}

.avatar {
  width: 62px;
  height: 62px;
  flex: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
  font-size: 26px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-line {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
}

.profile-line.sub {
  font-size: 12px;
  color: #909399;
}

.mini-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.06);
  border-left: 3px solid #409eff;
  height: 100%;
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
  overflow: hidden;
  text-overflow: ellipsis;
}

.mini-value.small {
  font-size: 15px;
  line-height: 26px;
}

.mini-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

.today-body {
  text-align: center;
  padding: 6px 0;
}

.today-status {
  display: inline-block;
  padding: 10px 26px;
  border-radius: 26px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}

.today-line {
  font-size: 13px;
  color: #909399;
}

.table-foot {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
  font-size: 13px;
  color: #606266;
  text-align: right;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px dashed #ebeef5;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.notice-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.notice-body {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
}

.notice-foot {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.empty-text {
  padding: 40px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
