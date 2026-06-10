import request from './request'

export function loginApi(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function registerApi(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function getUserInfoApi() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}
