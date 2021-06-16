<?php

use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

/*Route::get('/', function () {
    return view('welcome');
});*/

Route::get('/', 'PagesController@index'); //home page

Route::resource('metarules','MetarulesController');
//Route::get('/openhab-cloud', 'MetarulesController@openhab_cloud');
//Route::get('/openhab-local', 'MetarulesController@openhab_local');
Route::get('/testapi', 'MetarulesController@testapi');
Route::get('/results', 'MetarulesController@results');

Auth::routes();

Route::get('/home', 'HomeController@index')->name('home');



Route::resource('openhabrules','OpenhabrulesController');
Route::get('/openhab-cloud', 'OpenhabrulesController@openhab_cloud');
Route::get('/openhab-local', 'OpenhabrulesController@openhab_local');
Route::get('/retrieve-openhab-local', 'OpenhabrulesController@retrieve_openhab_local');
Route::get('/retrieve-openhab-cloud', 'OpenhabrulesController@retrieve_openhab_cloud');

Route::get('/turnLightsON', 'OpenhabrulesController@turnLightsON');
Route::get('/turnLightsOFF', 'OpenhabrulesController@turnLightsOFF');
Route::get('/turnTemperatureON', 'OpenhabrulesController@turnTemperatureON');
Route::get('/turnTemperatureOFF', 'OpenhabrulesController@turnTemperatureOFF');
