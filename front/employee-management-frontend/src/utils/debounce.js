/**
 * Returns a debounced version of fn that delays invocation
 * until after waitMs milliseconds have elapsed since the last call.
 *
 * @param {Function} fn - The function to debounce
 * @param {number} waitMs - Delay in milliseconds (default 300)
 * @returns {Function}
 */
export function debounce(fn, waitMs = 300) {
  let timer = null

  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      timer = null
      fn.apply(this, args)
    }, waitMs)
  }
}
