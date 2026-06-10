import request from './request'

export function generateContentApi(projectId, modelProvider) {
  return request({
    url: '/content/generate',
    method: 'post',
    data: { projectId, modelProvider }
  })
}

export function optimizeContentApi(projectId, section, instruction) {
  return request({
    url: '/content/optimize',
    method: 'post',
    data: { projectId, section, instruction }
  })
}

export function getContentApi(projectId) {
  return request({
    url: `/content/${projectId}`,
    method: 'get'
  })
}

export function regenerateStepApi(projectId, step, modelProvider) {
  return request({
    url: '/content/regenerate-step',
    method: 'post',
    data: { projectId, step, modelProvider }
  })
}

export function startGenerationApi(projectId, modelProvider) {
  return request({
    url: '/generation/start',
    method: 'post',
    data: { projectId, modelProvider }
  })
}

export function getGenerationProgressApi(projectId) {
  return request({
    url: `/generation/progress/${projectId}`,
    method: 'get'
  })
}

export function getScriptApi(projectId) {
  return request({
    url: `/script/${projectId}`,
    method: 'get'
  })
}

export function getSceneShotsApi(sceneId) {
  return request({
    url: `/script/scene/${sceneId}/shots`,
    method: 'get'
  })
}

export function getSceneMaterialsApi(sceneId) {
  return request({
    url: `/script/scene/${sceneId}/materials`,
    method: 'get'
  })
}
