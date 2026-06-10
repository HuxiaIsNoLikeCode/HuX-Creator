import request from './request'

export function exportMarkdownApi(projectId) {
  return request({
    url: `/export/markdown/${projectId}`,
    method: 'get',
    responseType: 'text'
  })
}

export function exportJsonApi(projectId) {
  return request({
    url: `/export/json/${projectId}`,
    method: 'get',
    responseType: 'text'
  })
}

export function exportWordApi(projectId) {
  return request({
    url: `/export/word/${projectId}`,
    method: 'get',
    responseType: 'blob'
  })
}
