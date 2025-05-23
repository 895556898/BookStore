// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },

  css: ['element-plus/dist/index.css'],


  modules: [
    '@nuxt/fonts',
    '@nuxt/icon',
    '@nuxt/ui',
    '@nuxt/content',
    '@nuxt/eslint',
    '@element-plus/nuxt',
    '@pinia/nuxt'
  ],

  elementPlus: {
    importStyle: 'scss',  // 推荐使用SCSS自定义主题
    // themes: ['dark']      // 可选暗黑主题
    // directives: PresetDirectives,
  },
  
  // API代理配置
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端服务器地址，根据实际情况修改
        changeOrigin: true,
        prependPath: true
      }
    }
  }
})