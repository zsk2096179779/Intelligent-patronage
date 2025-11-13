<template>
  <StrategyCombinationCreate :portfolio-id="portfolioId" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import StrategyCombinationCreate from '../../components/model3/StrategyCombinationCreate.vue'

const route = useRoute()
const portfolioId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})
</script>
