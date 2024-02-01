<script setup>
import {getUserGroup} from "@/views/scripts/UserUtils";

let userGroup = getUserGroup();

document.title = "饮用水配送管理" + " - HMS(" + userGroup + ")";

</script>

<template>
  SendingPage
</template>

<style scoped>

</style>