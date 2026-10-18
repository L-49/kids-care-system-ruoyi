import request from '@/utils/request'

export function listCourse(query) {
  return request({ url: '/kids/course/list', method: 'get', params: query })
}

export function getCourse(id) {
  return request({ url: '/kids/course/' + id, method: 'get' })
}

export function addCourse(data) {
  return request({ url: '/kids/course', method: 'post', data: data })
}

export function updateCourse(data) {
  return request({ url: '/kids/course', method: 'put', data: data })
}

export function delCourse(id) {
  return request({ url: '/kids/course/' + id, method: 'delete' })
}
