

    //Datamembers





    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
            switch(requestCode){
            case REQUEST_ENABLE_BTADMIN:{
            // If request is cancelled, the result arrays are empty.
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            return;
            }
            }
            case REQUEST_ENABLE_LOC:{
            // If request is cancelled, the result arrays are empty.
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            return;
            }
            }
            }
            }

    public void retrieveBoard(String macAddress) {

    }

    public void getPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.BLUETOOTH_ADMIN)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_ENABLE_BTADMIN);
            }
        }
    }







    public void connectToPolarDevice(final String deviceAddress, final String deviceName){
//        MainActivity.this.runOnUiThread(new Runnable() {
//            public void run() {
//                makeToast("Connected to PolarH7");
//                mPolarH7.setChecked(true);
//                mHelpText.setText("Press continue");
//            }
//        });
        //TODO finalize connection to PolarH7
        //TODO change mHelpText to just say connect to devices since the polar device tries to connect automatically

    }









}
