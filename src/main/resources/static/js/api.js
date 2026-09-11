(() => {
  const TOKEN_KEY = "softstore.jwt";
  const USER_KEY = "softstore.user";
  const readJson = value => { try { return JSON.parse(value); } catch { return null; } };
  const protectedApi = path => /^\/api\/v1\/(cart|orders|licenses)(\/|$)/.test(path)
      || /^\/api\/v1\/admin(\/|$)/.test(path);
  const auth = {
    token: () => localStorage.getItem(TOKEN_KEY),
    user: () => readJson(localStorage.getItem(USER_KEY)) || null,
    save: response => {
      if (typeof response?.token !== "string" || response.token.trim() === "") {
        throw new Error("Сервер не вернул JWT для входа.");
      }
      localStorage.setItem(TOKEN_KEY, response.token);
      localStorage.setItem(USER_KEY, JSON.stringify({ email: response.email, role: response.role }));
    },
    clear: () => { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); },
    isAdmin: () => ["ADMIN", "ROLE_ADMIN"].includes(auth.user()?.role)
  };
  async function apiFetch(url, options = {}) {
    const { publicRequest = false, ...fetchOptions } = options;
    const headers = new Headers(fetchOptions.headers || {});
    const token = auth.token();
    if (token && !publicRequest) headers.set("Authorization", `Bearer ${token}`);
    let body = fetchOptions.body;
    if (body && typeof body !== "string" && !(body instanceof FormData)) {
      headers.set("Content-Type", "application/json");
      body = JSON.stringify(body);
    }
    const response = await fetch(url, { ...fetchOptions, headers, body });
    const isJson = response.headers.get("content-type")?.includes("application/json");
    const payload = isJson ? await response.json() : null;
    if (!response.ok) {
      const path = new URL(url, window.location.origin).pathname;
      if (response.status === 401 && protectedApi(path)) {
        auth.clear();
        if (window.location.pathname !== "/auth") window.location.assign("/auth");
      }
      const error = new Error(payload?.message || "Не удалось выполнить запрос");
      error.status = response.status;
      error.payload = payload;
      throw error;
    }
    return payload;
  }
  window.storeApi = { apiFetch, auth };
})();
