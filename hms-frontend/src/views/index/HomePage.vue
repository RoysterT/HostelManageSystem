<script>
import {getUserGroup} from "@/views/scripts/UserUtils";

let userGroup = getUserGroup();

document.title = "欢迎使用宿舍管理系统HMS(" + userGroup + ")";
export default {
    name: 'Welcome',
};
</script>

<template>
    <div class="welcome-page" style="height: 92vh">
        <div class="welcome-content">
            <h1>欢迎来到</h1>
            <h1 class="system-title">学生宿舍管理系统</h1>
            <p>Hostile Manage System</p>
        </div>
    </div>
</template>

<style scoped>
body{
    height: 92vh;
    overflow: hidden;
}

.welcome-page {
    background: linear-gradient(135deg, #2e3192, #f7b93e);
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
}

.welcome-content {
    text-align: center;
    color: #fff;
}

.system-title {
    font-size: 36px;
    font-weight: bold;
    margin: 10px 0;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
    animation: scaleUp 1s ease-in-out infinite alternate;
}

@keyframes scaleUp {
    0% {
        transform: scale(1);
    }
    100% {
        transform: scale(1.05);
    }
}
</style>
