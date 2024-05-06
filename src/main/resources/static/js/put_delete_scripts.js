function rename(oldName, path) {
    const url = 'http://localhost/';

    if (path === null) {
        path = '';
    }

    const data = new URLSearchParams();
    data.append('newName', document.getElementById(oldName).value);
    data.append('oldName', oldName);
    data.append('path', path);


    const options = {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: data
    };

    fetch(url, options)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(html => {
            document.open();
            document.write(html);
            document.close();
        })
        .catch(error => {
            console.error('There was a problem with your fetch operation:', error);

        });
}

function remove(name, path) {
    const url = 'http://localhost/';

    if (path === null) {
        path = '';
    }

    const data = new URLSearchParams();

    data.append('name', name);
    data.append('path', path);


    console.log(data.get('name'));
    console.log(data.get('path'));

    const options = {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: data
    };

    fetch(url, options)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(html => {
            document.open();
            document.write(html);
            document.close();
        })
        .catch(error => {
            console.error('There was a problem with your fetch operation:', error);

        });
}
