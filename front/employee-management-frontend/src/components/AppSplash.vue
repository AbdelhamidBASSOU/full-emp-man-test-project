<template>
  <div class="splash">
    <div class="splash-sidebar">
      <div class="splash-brand">
        <div class="splash-brand-mark skeleton"></div>
        <div class="splash-brand-text skeleton"></div>
      </div>
      <div class="splash-nav">
        <div class="splash-nav-item skeleton"></div>
        <div class="splash-nav-item skeleton"></div>
      </div>
    </div>

    <div class="splash-main">
      <div class="splash-navbar">
        <div class="splash-navbar-title skeleton"></div>
        <div class="splash-navbar-actions">
          <div class="splash-avatar skeleton"></div>
        </div>
      </div>

      <div class="splash-content">
        <div class="splash-page-header">
          <div class="splash-heading skeleton"></div>
          <div class="splash-btn skeleton"></div>
        </div>
        <div class="splash-toolbar">
          <div class="splash-search skeleton"></div>
        </div>
        <div class="splash-table-card">
          <div class="splash-table-header">
            <div class="skeleton col-sm"></div>
            <div class="skeleton col-md"></div>
            <div class="skeleton col-md"></div>
            <div class="skeleton col-sm"></div>
            <div class="skeleton col-sm"></div>
            <div class="skeleton col-sm"></div>
          </div>
          <div
            v-for="i in 6"
            :key="i"
            class="splash-table-row"
            :style="{ animationDelay: `${i * 60}ms` }"
          >
            <div class="skeleton col-avatar"></div>
            <div class="skeleton col-md"></div>
            <div class="skeleton col-lg"></div>
            <div class="skeleton col-sm"></div>
            <div class="skeleton col-sm"></div>
            <div class="skeleton col-xs"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ── Layout mirrors AppLayout ────────────────────────────── */
.splash {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* ── Sidebar ──────────────────────────────────────────────── */
.splash-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--color-chrome);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}
.splash-brand {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-xs) 0;
}
.splash-brand-mark {
  width: 30px;
  height: 30px;
  border-radius: var(--radius);
  flex-shrink: 0;
}
.splash-brand-text {
  height: 14px;
  width: 100px;
  border-radius: var(--radius);
}
.splash-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.splash-nav-item {
  height: 36px;
  border-radius: var(--radius);
}

/* ── Main area ────────────────────────────────────────────── */
.splash-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.splash-navbar {
  height: 60px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  flex-shrink: 0;
}
.splash-navbar-title {
  height: 16px;
  width: 120px;
  border-radius: var(--radius);
}
.splash-navbar-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}
.splash-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

/* ── Content ──────────────────────────────────────────────── */
.splash-content {
  padding: var(--space-xl);
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
.splash-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}
.splash-heading {
  height: 24px;
  width: 160px;
  border-radius: var(--radius);
}
.splash-btn {
  height: 36px;
  width: 120px;
  border-radius: var(--radius);
}
.splash-toolbar {
  margin-bottom: var(--space-md);
}
.splash-search {
  height: 36px;
  width: 320px;
  border-radius: var(--radius);
}

/* ── Table skeleton ──────────────────────────────────────── */
.splash-table-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  overflow: hidden;
}
.splash-table-header {
  display: flex;
  gap: var(--space-xl);
  align-items: center;
  padding: 12px var(--space-md);
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg);
}
.splash-table-row {
  display: flex;
  gap: var(--space-xl);
  align-items: center;
  padding: 14px var(--space-md);
  border-bottom: 1px solid var(--color-border);
  animation: skeletonFade 1.6s ease-in-out infinite;
}
.splash-table-row:last-child {
  border-bottom: none;
}

/* ── Skeleton column helpers ─────────────────────────────── */
.skeleton {
  background: linear-gradient(
    90deg,
    var(--color-border) 25%,
    #e9ebf0 50%,
    var(--color-border) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.6s ease-in-out infinite;
  border-radius: var(--radius);
}
.col-avatar { width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0; }
.col-xs  { height: 14px; width: 48px;  flex-shrink: 0; }
.col-sm  { height: 14px; width: 72px;  flex-shrink: 0; }
.col-md  { height: 14px; width: 120px; flex-shrink: 0; }
.col-lg  { height: 14px; width: 180px; flex-shrink: 0; }

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
@keyframes skeletonFade {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.7; }
}
</style>
