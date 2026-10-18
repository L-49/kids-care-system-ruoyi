import request from '@/utils/request'

export function listFood(query) {
  return request({ url: '/kids/food/list', method: 'get', params: query })
}

// 库存概览
export function foodOverview() {
  return request({ url: '/kids/food/overview', method: 'get' })
}

export function getFood(id) {
  return request({ url: '/kids/food/' + id, method: 'get' })
}

export function addFood(data) {
  return request({ url: '/kids/food', method: 'post', data: data })
}

export function updateFood(data) {
  return request({ url: '/kids/food', method: 'put', data: data })
}

export function delFood(id) {
  return request({ url: '/kids/food/' + id, method: 'delete' })
}

// 出入库：delta 为增量（出库传负数）
export function changeStock(id, data) {
  return request({ url: '/kids/food/' + id + '/stock', method: 'post', data: data })
}

// 出入库流水
export function listStockRecord(query) {
  return request({ url: '/kids/food/record/list', method: 'get', params: query })
}
