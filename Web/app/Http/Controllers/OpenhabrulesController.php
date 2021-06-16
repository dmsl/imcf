<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Openhabrule;

class OpenhabrulesController extends Controller
{
  public function get_openhabrules()
    {
       $openhabrules = Openhabrule::get();
       return $openhabrules[0]->rules;
    }

    public function update_openhabrules(Request $request)
    {
       $openhabRules= Openhabrule::find(1);
       $openhabRules->rules=$request->input('rules');
       $openhabRules->save();      
       return $request->input('rules');


    }

  public function turnTemperatureOFF()
    {
       $openhabRules= Openhabrule::find(1);
       $openhabRulesTemp=  json_decode($openhabRules->rules);  
       
       foreach($openhabRulesTemp as $obj)
      {
         if ($obj->category =='Temperature')
         {           
           $obj->stateONOFF="OFF";
         }
      }

      $openhabRules->rules= json_encode($openhabRulesTemp);      
      $openhabRules->save();

    }

  public function turnTemperatureON()
    {
       $openhabRules= Openhabrule::find(1);
       $openhabRulesTemp=  json_decode($openhabRules->rules);  
       
       foreach($openhabRulesTemp as $obj)
      {
         if ($obj->category =='Temperature')
         {           
           $obj->stateONOFF="ON";
         }
      }

      $openhabRules->rules= json_encode($openhabRulesTemp);      
      $openhabRules->save();

    }

    public function turnLightsON()
    {
       $openhabRules= Openhabrule::find(1);
       $openhabRulesTemp=  json_decode($openhabRules->rules);  
       
       foreach($openhabRulesTemp as $obj)
      {
         if ($obj->category =='Light')
         {           
           $obj->stateONOFF="ON";
         }
      }

      $openhabRules->rules= json_encode($openhabRulesTemp);      
      $openhabRules->save();

    }

    public function turnLightsOFF()
    {
       $openhabRules= Openhabrule::find(1);
       $openhabRulesTemp=  json_decode($openhabRules->rules);  
       
       foreach($openhabRulesTemp as $obj)
      {
         if ($obj->category =='Light')
         {           
           $obj->stateONOFF="OFF";
         }
      }

      $openhabRules->rules= json_encode($openhabRulesTemp);      
      $openhabRules->save();

    }


    public function retrieve_openhab_local()
    {        
      ////$OpenHabResponse = file_get_contents('http://api.openweathermap.org/data/2.5/weather?q=Nicosia&appid=fb10b24aefe07de3506b4845ca1a7ef5');
      //$openhabRulesTemp = file_get_contents('http://192.168.10.1:8080/rest/items?recursive=false');
      $openhabRulesTemp = file_get_contents('http://10.16.30.73:8080/rest/items?recursive=false');
      $openhabRulesTemp = json_decode($openhabRulesTemp);
      
      foreach($openhabRulesTemp as $obj)
      {
         if ($obj->label !='')
         {           
           $obj->stateONOFF="ON";
         }
      }
      
     $openhabRules= Openhabrule::find(1);
       if (empty($openhabRules))
       {
         $openhabRules = new Openhabrule;
         $openhabRules->rules= json_encode($openhabRulesTemp);      
         $openhabRules->save();
       }
       else
       {         
         $openhabRules->rules= json_encode($openhabRulesTemp);      
         $openhabRules->save();
       }

      $openhabRules=  json_decode($openhabRules->rules);      

      return view('openhab.openhablocal')->with('openhabRules',$openhabRules);
    }

    public function openhab_local()
    {
      ////$OpenHabResponse = file_get_contents('http://api.openweathermap.org/data/2.5/weather?q=Nicosia&appid=fb10b24aefe07de3506b4845ca1a7ef5');
     /* $openhabRulesTemp = file_get_contents('http://192.168.10.1:8080/rest/items?recursive=false');
      $openhabRulesTemp = json_decode($openhabRulesTemp);
      
      foreach($openhabRulesTemp as $obj)
      {
         if ($obj->label !='')
         {           
           $obj->stateONOFF="ON";
         }
      }
      //return $openhabRules;

      return view('openhab.openhablocal')->with('openhabRules',$openhabRulesTemp);*/

      $openhabRules= Openhabrule::find(1);
       if (!empty($openhabRules))
       {
        $openhabRules=  json_decode($openhabRules->rules);  
       }
       else
       {
        $openhabRules=  json_decode("[]");
       }

      return view('openhab.openhablocal')->with('openhabRules',$openhabRules);

    }


     public function retrieve_openhab_cloud()
    {        
      //$openhabRules = file_get_contents('https://sconst01%40ucy.ac.cy:PASSWO@home.myopenhab.org/rest/items?recursive=false');
      $openhabRulesTemp = file_get_contents('https://sconst01@cs.ucy.ac.cy:PASSWO@home.myopenhab.org/rest/items?recursive=false');
      $openhabRulesTemp = json_decode($openhabRulesTemp);
      
      foreach($openhabRulesTemp as $obj)
      {
         if ($obj->label !='')
         {           
           $obj->stateONOFF="ON";
         }
      }
      
     $openhabRules= Openhabrule::find(1);
       if (empty($openhabRules))
       {
         $openhabRules = new Openhabrule;
         $openhabRules->rules= json_encode($openhabRulesTemp);      
         $openhabRules->save();
       }
       else
       {         
         $openhabRules->rules= json_encode($openhabRulesTemp);      
         $openhabRules->save();
       }

      $openhabRules=  json_decode($openhabRules->rules);      

      return view('openhab.openhabcloud')->with('openhabRules',$openhabRules);
    }

    public function openhab_cloud()
    {
      ////$openhabRules = file_get_contents('https://sconst01%40ucy.ac.cy:PASSWO@home.myopenhab.org/rest/items?recursive=false');
      /*$openhabRules = file_get_contents('https://sconst01@ucy.ac.cy:PASSWO@home.myopenhab.org/rest/items?recursive=false');
      $openhabRules = json_decode($openhabRules);

      foreach($openhabRules as $obj)
      {
         if ($obj->label !='')
         {           
           $obj->stateONOFF="ON";
         }
      }

      return view('openhab.openhabcloud')->with('openhabRules',$openhabRules);*/

      $openhabRules= Openhabrule::find(1);
       if (!empty($openhabRules))
       {
        $openhabRules=  json_decode($openhabRules->rules);  
       }
       else
       {
        $openhabRules=  json_decode("[]");
       }

      return view('openhab.openhabcloud')->with('openhabRules',$openhabRules);

    }


    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        //
    }

    /**
     * Show the form for creating a new resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function store(Request $request)
    {
        //
    }

    /**
     * Display the specified resource.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function show($id)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function edit($id)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function update(Request $request, $id)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function destroy($id)
    {
        //
    }
}


//[{"members":[],"link":"http:\/\/10.16.30.73:8080\/rest\/items\/Garden","state":"NULL","editable":false,"type":"Group","name":"Garden","label":"Garden","category":"garden","tags":["Garden"],"groupNames":[],"stateONOFF":"ON"},{"members":[],"link":"http:\/\/10.16.30.73:8080\/rest\/items\/gGF","state":"NULL","editable":false,"type":"Group","name":"gGF","label":"Ground Floor","category":"groundfloor","tags":["GroundFloor"],"groupNames":[],"stateONOFF":"ON"},{"link":"http:\/\/10.16.30.73:8080\/rest\/items\/FirstFloorBedroom_Current_Cloudiness","state":"75 %","stateDescription":{"minimum":0,"maximum":100,"pattern":"%d %unit%","readOnly":true,"options":[]},"editable":true,"type":"Number:Dimensionless","name":"FirstFloorBedroom_Current_Cloudiness","label":"Bedroom Lights","category":"Light","tags":[],"groupNames":["gFF"],"stateONOFF":"ON"},{"link":"http:\/\/10.16.30.73:8080\/rest\/items\/GroudFloorKitchen_Current_OutdoorTemperature","state":"17.66 \u00b0C","stateDescription":{"pattern":"%.1f %unit%","readOnly":true,"options":[]},"editable":true,"type":"Number:Temperature","name":"GroudFloorKitchen_Current_OutdoorTemperature","label":"Kitchen Air-Conditioner","category":"Temperature","tags":[],"groupNames":["gGF"],"stateONOFF":"ON"},{"members":[],"link":"http:\/\/10.16.30.73:8080\/rest\/items\/gFF","state":"NULL","editable":false,"type":"Group","name":"gFF","label":"First Floor","category":"firstfloor","tags":["FirstFloor"],"groupNames":[],"stateONOFF":"ON"},{"members":[],"link":"http:\/\/10.16.30.73:8080\/rest\/items\/imcf","state":"NULL","editable":false,"type":"Group","name":"imcf","label":"IMCF","category":"imcf","tags":["IMCF"],"groupNames":[],"stateONOFF":"ON"},{"link":"http:\/\/10.16.30.73:8080\/rest\/items\/WeatherAndForecast_Current_Cloudiness","state":"20 %","stateDescription":{"minimum":0,"maximum":100,"pattern":"%d %unit%","readOnly":true,"options":[]},"editable":true,"type":"Number:Dimensionless","name":"WeatherAndForecast_Current_Cloudiness","label":"Living Room Lights","category":"Light","tags":[],"groupNames":["gGF"],"stateONOFF":"ON"},{"link":"http:\/\/10.16.30.73:8080\/rest\/items\/WeatherAndForecast_Current_OutdoorTemperature","state":"18.93 \u00b0C","stateDescription":{"pattern":"%.1f %unit%","readOnly":true,"options":[]},"editable":true,"type":"Number:Temperature","name":"WeatherAndForecast_Current_OutdoorTemperature","label":"Living room Air-Conditioner","category":"Temperature","tags":[],"groupNames":["gGF"],"stateONOFF":"ON"},{"link":"http:\/\/10.16.30.73:8080\/rest\/items\/GroudFloorKitchen_Current_Cloudiness","state":"75 %","stateDescription":{"minimum":0,"maximum":100,"pattern":"%d %unit%","readOnly":true,"options":[]},"editable":true,"type":"Number:Dimensionless","name":"GroudFloorKitchen_Current_Cloudiness","label":"Kitchen Lights","category":"Light","tags":[],"groupNames":["gGF"],"stateONOFF":"ON"},{"link":"http:\/\/10.16.30.73:8080\/rest\/items\/FirstFloorBedroom_Current_OutdoorTemperature","state":"20.0 \u00b0C","stateDescription":{"pattern":"%.1f %unit%","readOnly":true,"options":[]},"editable":true,"type":"Number:Temperature","name":"FirstFloorBedroom_Current_OutdoorTemperature","label":"Bedroom Air-Conditioner","category":"Temperature","tags":[],"groupNames":["gFF"],"stateONOFF":"ON"}]