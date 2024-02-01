<script>
export default {
  name: 'NotFound',
};
</script>

<template>
  <div class="not-found">
    <h1>404 Error</h1>
    <p>您访问的页面<span>{{ $route.fullPath }}</span>不存在</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<style scoped>
.not-found {
  text-align: center;
  margin-top: 100px;
}

h1 {
  font-size: 36px;
  color: #333;
  margin-bottom: 20px;
}

p {
  font-size: 18px;
  color: #777;
  margin-bottom: 20px;
}

router-link {
  font-size: 16px;
  color: #007bff;
  text-decoration: underline;
}
</style>
