import request from '@/utils/request'

// 查询教职工列表
export function listTeacher(query) {
  return request({ url: '/kids/teacher/list', method: 'get', params: query })
}

// 在职教职工（下拉框用）
export function listTeacherAll() {
  return request({ url: '/kids/teacher/all', method: 'get' })
}

// 教职工概览
export function teacherOverview() {
  return request({ url: '/kids/teacher/overview', method: 'get' })
}

// 教职工详情
export function getTeacher(id) {
  return request({ url: '/kids/teacher/' + id, method: 'get' })
}

// 新增教职工
export function addTeacher(data) {
  return request({ url: '/kids/teacher', method: 'post', data: data })
}

// 修改教职工
export function updateTeacher(data) {
  return request({ url: '/kids/teacher', method: 'put', data: data })
}

// 删除教职工
export function delTeacher(id) {
  return request({ url: '/kids/teacher/' + id, method: 'delete' })
}

// 任教班级
export function teacherClasses(id) {
  return request({ url: '/kids/teacher/' + id + '/classes', method: 'get' })
}

// 任教课程
export function teacherCourses(id) {
  return request({ url: '/kids/teacher/' + id + '/courses', method: 'get' })
}

// 设为班主任
export function setHeadTeacher(id, clazzId, replace) {
  return request({
    url: '/kids/teacher/' + id + '/head-teacher/' + clazzId,
    method: 'put',
    params: { replace: replace === true }
  })
}

// 取消班主任
export function removeHeadTeacher(id, clazzId) {
  return request({ url: '/kids/teacher/' + id + '/head-teacher/' + clazzId, method: 'delete' })
}
