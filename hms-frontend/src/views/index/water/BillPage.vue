<script setup>
import {getUserGroup} from "@/views/scripts/UserUtils";

let userGroup = getUserGroup();

document.title = "饮用水订购" + " - HMS(" + userGroup + ")";

</script>

<template>
  BillPage
</template>

<style scoped>

</style>