package org.cnodejs.android.md.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.umeng.analytics.MobclickAgent;

import org.cnodejs.android.md.R;
import org.cnodejs.android.md.model.api.ApiClient;
import org.cnodejs.android.md.model.entity.LoginInfo;
import org.cnodejs.android.md.storage.LoginShared;
import org.cnodejs.android.md.ui.base.StatusBarActivity;
import org.cnodejs.android.md.ui.dialog.DialogUtils;
import org.cnodejs.android.md.ui.dialog.ProgressDialog;
import org.cnodejs.android.md.ui.listener.NavigationFinishClickListener;
import org.cnodejs.android.md.ui.widget.ThemeUtils;
import org.cnodejs.android.md.ui.widget.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends StatusBarActivity {

    private static final int REQUEST_QRCODE = 100;

    @Bind(R.id.login_toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.login_edt_access_token)
    protected MaterialEditText edtAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.configThemeBeforeOnCreate(this, R.style.AppThemeLight, R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        toolbar.setNavigationOnClickListener(new NavigationFinishClickListener(this));
    }

    @OnClick(R.id.login_btn_login)
    protected void onBtnLoginClick() {
        if (edtAccessToken.getText().length() < 36) {
            edtAccessToken.setError(getString(R.string.access_token_format_error_tip));
        } else {

            final ProgressDialog dialog = DialogUtils.createProgressDialog(this);
            dialog.setMessage(R.string.logging_in_$_);
            dialog.show();

            final String accessToken = edtAccessToken.getText().toString();

            ApiClient.service.accessToken(accessToken, new Callback<LoginInfo>() {

                @Override
                public void success(LoginInfo loginInfo, Response response) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        LoginShared.login(LoginActivity.this, accessToken, loginInfo);
                        ToastUtils.with(LoginActivity.this).show(R.string.login_success);
                        setResult(RESULT_OK);
                        finish();

                        MobclickAgent.onProfileSignIn(loginInfo.getLoginName()); // 开始友盟账号统计
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        if (error.getResponse() != null && error.getResponse().getStatus() == 403) {
                            edtAccessToken.setError(getString(R.string.access_token_error));
                        } else {
                            ToastUtils.with(LoginActivity.this).show(R.string.network_faild);
                        }
                    }
                }

            });

        }
    }

    @OnClick(R.id.login_btn_qrcode)
    protected void onBtnQrcodeClick() {
        startActivityForResult(new Intent(this, QRCodeActivity.class), REQUEST_QRCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_QRCODE && resultCode == RESULT_OK) {
            edtAccessToken.setText(data.getStringExtra(QRCodeActivity.EXTRA_QRCODE));
            edtAccessToken.setSelection(edtAccessToken.length());
        }
    }

}
