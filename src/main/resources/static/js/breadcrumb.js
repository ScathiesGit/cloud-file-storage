function sendRequest(path) {
    if (path === '/') {
        window.location.href = '/';
    } else {
        window.location.href = '/?path=' + encodeURIComponent(path);
    }
}