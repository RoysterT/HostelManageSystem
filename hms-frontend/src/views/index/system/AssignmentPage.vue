<script setup>
import {getUserGroup} from "@/views/scripts/UserUtils";

let userGroup = getUserGroup();

document.title = "用户所属组管理" + " - HMS(" + userGroup + ")";
</script>

<template>
  AssignmentPage
</template>

<style scoped>

</style>