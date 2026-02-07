import http from '../axios-project'

export default {
  pageStory(params) {
    return http({
      url: '/story/page',
      method: 'get',
      params
    })
  },
  createStory(data) {
    return http({
      url: '/story',
      method: 'post',
      data
    })
  },
  clearMyStory() {
    return http({
      url: '/story/clear',
      method: 'post'
    })
  },
  deleteStory(data) {
    return http({
      url: '/story/delete',
      method: 'post',
      data
    })
  },
  likeStory(data) {
    return http({
      url: '/story/like',
      method: 'post',
      data
    })
  }
}
