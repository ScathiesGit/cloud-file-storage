function rename(oldName, path, isFolder, parentFolderPath, parentFolderName) {
    const url = 'http://localhost/';


    const data = new URLSearchParams();
    data.append('newName', document.getElementById(oldName).value);
    data.append('oldName', oldName);
    data.append('isFolder', isFolder)
    data.append('pathToDirectory', path);

    if (parentFolderPath !== null) {

        data.append('path', parentFolderPath);
    }

    if (parentFolderName !== null) {

        data.append('name', parentFolderName);
    }

    console.log('Storage item name for rename: ' + oldName)
    console.log('Path to storage item: ' + path)
    console.log('is Folder: ' + isFolder)
    console.log('Current folder: ' + parentFolderPath)
    console.log('Name parent folder for current folder: ' + parentFolderName)

    const options = {
        method: 'PATCH',
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

function remove(removalItemName, path, isFolder, name) {
    const url = 'http://localhost/';

    const data = new URLSearchParams();

    if (name !== null) {
        data.append('name', name);
        data.append('parentFolder', name);
    }

    if (path !== null) {
        data.append('path', path);
    }

    data.append('isFolder', isFolder)
    data.append('removalItemName', removalItemName)


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
