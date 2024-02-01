<script setup>

import {useDark, useToggle} from "@vueuse/core";
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light'
})

useDark({
  onChanged(dark){ useToggle((dark))}
})
</script>

<template>
  <div>
    <router-view />
  </div>
</template>

<style scoped>

</style>
