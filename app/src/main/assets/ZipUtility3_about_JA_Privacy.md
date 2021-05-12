## 1.収集データ
### 1.1.ユーザーからZipUtility3に提供されるデータ

暗号化ZIPファイルの作成と解凍のためのパスワードはアプリが終了時に廃棄され保存されることはありません。

### 1.2.ZipUtility3の活動記録

ログを有効にした場合、ZipUtility3の実行結果の検証と技術サポートのために活動記録データをアプリ内の記憶領域に保存します。
<span style="color: red;"><u>データは”1.3.ZipUtility3外へのデータの送信または書出し”の操作が行われない限り外部に送信されません。</u></span>

- デバイス情報(メーカー名、モデル名、OSのバージョン, マウントポイント, アプリ固有ディレクトリー, StorageAccessFramework, Storage manager)
- ZipUtility3のバージョン、ZipUtility3の実行オプション
- ディレクトリー名、ファイル名、実行状況
- デバッグ情報
- エラー情報

### 1.3.ZipUtility3外へのデータの送信または書出し

ユーザーが操作しない限りZipUtility3が保有するデータは外部に送信または書出しは行いません。

- 履歴タブから「共有ボタン」を押す
- システム情報から「開発者に送る」ボタンを押す
- ログ管理から「開発者に送る」ボタンを押す
- ログ管理から「ログファイルの書出」ボタンを押すことにより外部ストレージに書き出します

### 1.4.ZipUtility3内に保存されたデータの削除

ZipUtility3をアンインストールする事により保存したデータ("1.2.ZipUtility3の活動記録")はデバイスから削除されます。
<span style="color: red; "><u>ただし、ユーザーの操作により外部ストレージに保存されたデータは削除されません。</u></span>

## 2.アプリ実行に必要な権限

### 2.1.写真、メディア、ファイル
**read the contents of your USB storage**
**modify or delete the contents of your USB storage**
ファイル同期と管理ファイルの読み書きで使用します。

### 2.2.ストレージ

### 2.2.1.Android11以降
**All file access**

ファイル同期と管理ファイルの読み書きで使用します。

### 2.2.2.Android10以前
**read the contents of your USB storage**
**modify or delete the contents of your USB storage**
ファイル同期と管理ファイルの読み書きで使用します。

### 2.3.その他

### 2.3.1.Prevent device from sleeping
スケジュールまたは外部アプリからの同期開始で使用します。
