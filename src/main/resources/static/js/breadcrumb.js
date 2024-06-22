function sendRequest(path, name) {
    if (path === '') {
        window.location.href = '/';
    } else {
        window.location.href = '/?path=' + encodeURIComponent(path) + '&name=' + encodeURIComponent(name);
    }
}