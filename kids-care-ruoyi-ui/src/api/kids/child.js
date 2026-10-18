import request from '@/utils/request'

// 查询幼儿档案列表
export function listChild(query) {
  return request({ url: '/kids/child/list', method: 'get', params: query })
}

// 查询在园幼儿（下拉框用）
export function listChildAll() {
  return request({ url: '/kids/child/all', method: 'get' })
}

// 查询有过敏史的幼儿
export function listAllergyChild() {
  return request({ url: '/kids/child/allergy', method: 'get' })
}

// 查询幼儿档案详情
export function getChild(id) {
  return request({ url: '/kids/child/' + id, method: 'get' })
}

// 新增幼儿档案
export function addChild(data) {
  return request({ url: '/kids/child', method: 'post', data: data })
}

// 修改幼儿档案
export function updateChild(data) {
  return request({ url: '/kids/child', method: 'put', data: data })
}

// 删除幼儿档案
export function delChild(id) {
  return request({ url: '/kids/child/' + id, method: 'delete' })
}

// 调班
export function transferChild(id, targetClazzId) {
  return request({ url: '/kids/child/' + id + '/transfer', method: 'post', data: { targetClazzId } })
}

// 档案变更记录
export function childHistory(id) {
  return request({ url: '/kids/child/' + id + '/history', method: 'get' })
}

// 下载导入模板
export function importTemplate() {
  return request({ url: '/kids/child/import/template', method: 'post', responseType: 'blob' })
}

// 导入幼儿档案
export function importChild(data) {
  return request({
    url: '/kids/child/import',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
