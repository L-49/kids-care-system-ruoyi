import request from '@/utils/request'

export function listNotice(query) {
  return request({ url: '/kids/notice/list', method: 'get', params: query })
}

// 全部已发布公告（家长端用）
export function listAllNotice() {
  return request({ url: '/kids/notice/all', method: 'get' })
}

export function getNotice(id) {
  return request({ url: '/kids/notice/' + id, method: 'get' })
}

export function addNotice(data) {
  return request({ url: '/kids/notice', method: 'post', data: data })
}

export function updateNotice(data) {
  return request({ url: '/kids/notice', method: 'put', data: data })
}

export function delNotice(id) {
  return request({ url: '/kids/notice/' + id, method: 'delete' })
}

// 立即发布
export function publishNotice(id) {
  return request({ url: '/kids/notice/' + id + '/publish', method: 'post' })
}

// 撤回
export function revokeNotice(id) {
  return request({ url: '/kids/notice/' + id + '/revoke', method: 'post' })
}
