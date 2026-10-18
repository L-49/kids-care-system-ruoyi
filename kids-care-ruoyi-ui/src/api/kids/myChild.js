import request from '@/utils/request'

// 家长端聚合：档案 + 考勤 + 账单 + 请假 + 公告
export function myChildInfo() {
  return request({ url: '/kids/my-child/info', method: 'get' })
}

// 家长端考勤明细
export function myChildAttendance() {
  return request({ url: '/kids/my-child/attendance', method: 'get' })
}

// 家长为绑定的幼儿提交请假申请
export function applyMyLeave(data) {
  return request({ url: '/kids/my-child/leave', method: 'post', data })
}
