// Ponte JavaScript de backup no Google Drive para a versão web.
// Usa Google Identity Services (token OAuth) + Drive REST API (appDataFolder).
//
// >>> Configure abaixo o OAuth Client ID do tipo "Web" criado no Google Cloud
// Console, com a Drive API habilitada e o escopo drive.appdata.
(function () {
    const CLIENT_ID = "849833328768-lgebbdo99v4jo1fjrk20667h005gb0ri.apps.googleusercontent.com";
    const SCOPE = "https://www.googleapis.com/auth/drive.appdata";
    const BACKUP_NAME = "meu_glossario_backup.json";

    let accessToken = null;
    let accountEmail = null;
    let tokenClient = null;

    function ensureTokenClient() {
        if (tokenClient) return tokenClient;
        if (!window.google || !google.accounts || !google.accounts.oauth2) return null;
        tokenClient = google.accounts.oauth2.initTokenClient({
            client_id: CLIENT_ID,
            scope: SCOPE,
            callback: () => {}
        });
        return tokenClient;
    }

    function signIn() {
        return new Promise((resolve, reject) => {
            const client = ensureTokenClient();
            if (!client) { reject("Google Identity Services indisponível."); return; }
            client.callback = (resp) => {
                if (resp && resp.access_token) {
                    accessToken = resp.access_token;
                    accountEmail = "conta Google conectada";
                    resolve(true);
                } else {
                    reject("Falha na autenticação.");
                }
            };
            client.requestAccessToken({ prompt: accessToken ? "" : "consent" });
        });
    }

    async function findBackupId() {
        const q = encodeURIComponent("name = '" + BACKUP_NAME + "'");
        const url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=" + q + "&fields=files(id,name)";
        const res = await fetch(url, { headers: { Authorization: "Bearer " + accessToken } });
        const data = await res.json();
        return (data.files && data.files.length > 0) ? data.files[0].id : null;
    }

    async function upload(content) {
        const existingId = await findBackupId();
        const metadata = existingId ? {} : { name: BACKUP_NAME, parents: ["appDataFolder"] };
        const boundary = "-------meuglossario";
        const body =
            "--" + boundary + "\r\n" +
            "Content-Type: application/json; charset=UTF-8\r\n\r\n" +
            JSON.stringify(metadata) + "\r\n" +
            "--" + boundary + "\r\n" +
            "Content-Type: application/json\r\n\r\n" +
            content + "\r\n" +
            "--" + boundary + "--";
        const method = existingId ? "PATCH" : "POST";
        const url = existingId
            ? "https://www.googleapis.com/upload/drive/v3/files/" + existingId + "?uploadType=multipart"
            : "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart";
        await fetch(url, {
            method: method,
            headers: {
                Authorization: "Bearer " + accessToken,
                "Content-Type": "multipart/related; boundary=" + boundary
            },
            body: body
        });
        return true;
    }

    async function download() {
        const id = await findBackupId();
        if (!id) return null;
        const res = await fetch(
            "https://www.googleapis.com/drive/v3/files/" + id + "?alt=media",
            { headers: { Authorization: "Bearer " + accessToken } }
        );
        return await res.text();
    }

    window.MeuGlossarioDrive = {
        isConnected: function () { return accessToken !== null; },
        account: function () { return accountEmail; },
        signIn: signIn,
        upload: upload,
        download: download
    };
})();
