<script setup>

</script>

<template>
    <div style="width: 100vw; height: 100vh; overflow: hidden; display: flex">
        <div style="flex: 1; background-color: blue;">
            <el-image
                style="width: 100%; height: 100%"
                fit=cover
                src="https://api.kdcc.cn"/>
        </div>
        <div class="welcome-title">
            <div>欢迎使用学生宿舍管理系统！</div>
        </div>
        <div class="right-card">
            <router-view v-slot="{Component}">
                <transition name="el-fade-in-linear" mode="out-in">
                    <Component :is="Component"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<style scoped>
@import "./style/welcome.css";
</style>
