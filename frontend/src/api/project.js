import request from './request'

export function createProjectApi(data) {
  return request({
    url: '/project/create',
    method: 'post',
    data
  })
}

export function getProjectsApi() {
  return request({
    url: '/project/list',
    method: 'get'
  })
}

export function getProjectDetailApi(id) {
  return request({
    url: `/project/${id}`,
    method: 'get'
  })
}

export function updateProjectApi(id, data) {
  return request({
    url: `/project/${id}`,
    method: 'put',
    data
  })
}

export function deleteProjectApi(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}
