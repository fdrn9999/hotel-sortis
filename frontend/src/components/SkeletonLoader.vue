<template>
  <div class="skeleton-loader" :class="{ 'skeleton-card': variant === 'card', 'skeleton-list': variant === 'list' }">
    <!-- Card variant: Grid of skeleton cards -->
    <template v-if="variant === 'card'">
      <div v-for="i in count" :key="i" class="skeleton-card-item">
        <div class="skeleton-badge skeleton-shimmer"></div>
        <div class="skeleton-title skeleton-shimmer"></div>
        <div class="skeleton-text skeleton-shimmer"></div>
        <div class="skeleton-text skeleton-text-short skeleton-shimmer"></div>
        <div class="skeleton-footer">
          <div class="skeleton-price skeleton-shimmer"></div>
        </div>
      </div>
    </template>

    <!-- List variant: Stacked skeleton rows -->
    <template v-else-if="variant === 'list'">
      <div v-for="i in count" :key="i" class="skeleton-list-item">
        <div class="skeleton-avatar skeleton-shimmer"></div>
        <div class="skeleton-content">
          <div class="skeleton-title skeleton-shimmer"></div>
          <div class="skeleton-text skeleton-shimmer"></div>
        </div>
      </div>
    </template>

    <!-- Text variant: Simple text lines -->
    <template v-else-if="variant === 'text'">
      <div v-for="i in count" :key="i" class="skeleton-text-line skeleton-shimmer" :style="{ width: getRandomWidth() }"></div>
    </template>

    <!-- Default/custom variant -->
    <template v-else>
      <slot>
        <div class="skeleton-default">
          <div class="skeleton-header skeleton-shimmer"></div>
          <div class="skeleton-body">
            <div class="skeleton-text skeleton-shimmer"></div>
            <div class="skeleton-text skeleton-shimmer"></div>
            <div class="skeleton-text skeleton-text-short skeleton-shimmer"></div>
          </div>
        </div>
      </slot>
    </template>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  variant?: 'card' | 'list' | 'text' | 'default'
  count?: number
}>(), {
  variant: 'default',
  count: 3
})

const { variant, count } = props

function getRandomWidth(): string {
  const widths = ['60%', '75%', '90%', '80%', '70%']
  return widths[Math.floor(Math.random() * widths.length)]
}
</script>

<style scoped>
.skeleton-loader {
  width: 100%;
}

/* Shimmer animation - Art Deco gold gradient */
.skeleton-shimmer {
  background: linear-gradient(
    90deg,
    rgba(var(--color-gold-rgb), 0.08) 0%,
    rgba(var(--color-gold-rgb), 0.15) 50%,
    rgba(var(--color-gold-rgb), 0.08) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
  border-radius: 4px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* Card variant */
.skeleton-card {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.skeleton-card-item {
  background: rgba(var(--color-dark-navy-rgb), 0.6);
  border: 1px solid rgba(var(--color-gold-rgb), 0.2);
  border-radius: 12px;
  padding: 16px;
}

.skeleton-badge {
  width: 60px;
  height: 14px;
  margin-bottom: 12px;
}

.skeleton-title {
  width: 80%;
  height: 20px;
  margin-bottom: 8px;
}

.skeleton-text {
  width: 100%;
  height: 14px;
  margin-bottom: 6px;
}

.skeleton-text-short {
  width: 60%;
}

.skeleton-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.skeleton-price {
  width: 80px;
  height: 18px;
}

/* List variant */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: rgba(var(--color-dark-navy-rgb), 0.4);
  border: 1px solid rgba(var(--color-gold-rgb), 0.15);
  border-radius: 8px;
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-content .skeleton-title {
  width: 40%;
  height: 16px;
  margin-bottom: 0;
}

.skeleton-content .skeleton-text {
  width: 70%;
  height: 12px;
  margin-bottom: 0;
}

/* Text variant */
.skeleton-text-line {
  height: 16px;
  margin-bottom: 12px;
}

.skeleton-text-line:last-child {
  margin-bottom: 0;
}

/* Default variant */
.skeleton-default {
  padding: 16px;
}

.skeleton-header {
  width: 50%;
  height: 24px;
  margin-bottom: 20px;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Tablet responsive */
@media (max-width: 768px) {
  .skeleton-card {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .skeleton-card-item {
    padding: 12px;
  }

  .skeleton-avatar {
    width: 40px;
    height: 40px;
  }
}

/* Mobile responsive */
@media (max-width: 480px) {
  .skeleton-card {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .skeleton-list-item {
    padding: 10px;
    gap: 12px;
  }
}
</style>
