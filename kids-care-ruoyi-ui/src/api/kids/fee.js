import request from '@/utils/request'

export function listFee(query) {
  return request({ url: '/kids/fee/list', method: 'get', params: query })
}

export function getFee(id) {
  return request({ url: '/kids/fee/' + id, method: 'get' })
}

export function addFee(data) {
  return request({ url: '/kids/fee', method: 'post', data: data })
}

export function updateFee(data) {
  return request({ url: '/kids/fee', method: 'put', data: data })
}

export function delFee(id) {
  return request({ url: '/kids/fee/' + id, method: 'delete' })
}

// 缴费登记
export function payFee(id) {
  return request({ url: '/kids/fee/pay/' + id, method: 'put' })
}
