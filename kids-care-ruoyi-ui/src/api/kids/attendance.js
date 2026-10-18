import request from '@/utils/request'

export function listAttendance(query) {
  return request({ url: '/kids/attendance/list', method: 'get', params: query })
}

export function getAttendance(id) {
  return request({ url: '/kids/attendance/' + id, method: 'get' })
}

export function addAttendance(data) {
  return request({ url: '/kids/attendance', method: 'post', data: data })
}

export function updateAttendance(data) {
  return request({ url: '/kids/attendance', method: 'put', data: data })
}

export function delAttendance(id) {
  return request({ url: '/kids/attendance/' + id, method: 'delete' })
}
