
@extends('layouts.app')
  @section('content')
  <div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
            <a href="{{ URL::previous() }}" class="btn btn-primary">Go Back</a>
            <br><br>

            <iframe width="100%" height="500" frameborder="0" scrolling="yes" marginheight="0" marginwidth="0" src="https://anyplace.cs.ucy.ac.cy/viewer/?buid=building_6aec231a-59d5-420d-9eb8-eabf6b6985c8_1605541987180&floor=0&selected=poi_57a73641-967e-4217-a149-bbe0a98be415"></iframe>

              <div class="card">
                  <div class="card-header">OpenHAB Configurations - Cloud Controller</div>

                  <div class="card-body">
                      <div class="panel-body">
                        <div>
                          <div class="float-md-left"><h1>OpenHAB Rules Table</h1> </div>
                          <div class="float-md-right"><a href="/retrieve-openhab-cloud" class="btn btn-primary">Retrieve OpenHAB Rules</a></div>
                        </div>

                        @if (count($openhabRules)>0)
                        <table class="table table-striped">
                            <tr>
                              <th>Description</th>
                              <th>Type</th>
                              <th>State</th>
                              <th>Mode</th>
                            </tr>
                            @foreach($openhabRules as $openhabRule)
                              @if ($openhabRule->type != 'Group')
                                <tr>
                                  <td> <!--- {{$openhabRule->name}} --->
                                    <script>
                                      document.write("{{$openhabRule->label}}".replace(/_/g, ' '));
                                    </script>
                                  </td>
                                  <td>                                  
                                      {{$openhabRule->category}} 
                                  </td>
                                  <td>
                                    <!---@if ($openhabRule->state == 'ON')
                                      <button type="button" class="btn btn-success">ON</button>
                                    @elseif ($openhabRule->state == 'OFF')
                                      <button type="button" class="btn btn-danger">OFF</button>
                                    @elseif ($openhabRule->type == 'DateTime')
                                      NULL
                                    @elseif (($openhabRule->name == 'ImageURL') ||  ($openhabRule->type == 'Location'))
                                        NULL
                                    @else
                                      {{$openhabRule->state}}
                                    @endif --->
                                    {{$openhabRule->state}}
                                  </td>
                                  <td>
                                    @if ($openhabRule->stateONOFF == 'ON')
                                      <button type="button" class="btn btn-success">ON</button>
                                    @elseif ($openhabRule->stateONOFF == 'OFF')
                                      <button type="button" class="btn btn-danger">OFF</button>
                                    @endif
                                  </td>
                                </tr>
                              @endif 
                            @endforeach
                        </table>
                        @endif

                      </div>
                  </div>
              </div>
          </div>
      </div>
  </div>


<script>

setTimeout(function(){
   window.location.reload(1);
}, 3000000); //3000);

</script>


  <!--- <div id="demo">
  <h1>The XMLHttpRequesdt Object</h1>
  <button type="button" onclick="loadDoc()">Change Content</button>
  </div>

  <script>
  function loadDoc() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
        document.getElementById("demo").innerHTML =
        this.responseText;
      }
    };
    xhttp.open("GET", "http://192.168.10.1:8080/rest/items?recursive=false", true);
    xhttp.send();
  }
  </script>
--->


  @endsection
