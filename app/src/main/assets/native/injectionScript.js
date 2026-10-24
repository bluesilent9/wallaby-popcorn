(() => {
    if (!document.getElementById('wallaby-popcorn-theme')) {
        const theme = document.createElement('link');
        theme.id = 'wallaby-popcorn-theme';
        theme.rel = 'stylesheet';
        theme.href = '/native/wallaby-popcorn.css';
        document.head.appendChild(theme);
    }
    const scripts = [
        '/native/wallaby-branding.js',
        '/native/nativeshell.js',
        '/native/EventEmitter.js',
        document.currentScript.src.concat('?deferred=true&ts=', Date.now())
    ];
    for (const script of scripts) {
        const scriptElement = document.createElement('script');
        scriptElement.src = script;
        scriptElement.charset = 'utf-8';
        scriptElement.setAttribute('defer', '');
        document.body.appendChild(scriptElement);
    }
    document.currentScript.remove();
})();
