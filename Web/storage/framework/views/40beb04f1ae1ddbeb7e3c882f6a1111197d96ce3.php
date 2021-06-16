  <?php $__env->startSection('content'); ?>
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
  <div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
             <h1><u>IoT Meta-Control Firewall (IMCF) - Results</u></h1>
            <div id="chart_div" style="width: 650px; height: 250px;"></div>
            <div id="chart_div2" style="width: 700px; height: 500px;"></div>
              <div class="card">

                  <div class="card-header">IMCF Results</div>

                  <div class="card-body">
                      <?php if(session('status')): ?>
                          <div class="alert alert-success" role="alert">
                              <?php echo e(session('status')); ?>

                          </div>
                      <?php endif; ?>

                      <!---You are logged in!--->

                      <div class="panel-body">
                        <div>
                          <div class="float-md-left"><h2><strong>Energy Planner - Results</strong></h2> </div>
                          <div class="float-md-right"><button type="button" class="btn btn-primary" onClick="window.location.reload();">Refresh Page</button> </div>
                        </div>
                       

                        <table class="table table-striped">
                              <tr>
                                <th><h5><strong>Description</strong></h5></th>                                
                                <th><h5><strong>Value</strong></h5></th> 
                              </tr>
                              <tr>
                                <td><h5>Kilowatt hour (kWh):</h5></td>
                                <td><h5><?php echo e($results[0]->ep_kWh); ?> kWh</h5></td>
                              </tr>
                              <tr>
                                <td><h5>Total Average Error:</h5></td>
                                <td>
                                  <h5>
                                    <script>
                                      var Total_Average_Error=<?php echo e($results[0]->ep_totalError); ?>;
                                      document.write(Total_Average_Error.toFixed(2));
                                    </script>
                                    %
                                  </h5>
                                </td>
                              </tr>                              
                              <tr>
                                <td><h5>Convenience Average Error:</h5></td>
                                <td>
                                  <h5>
                                    <script>
                                      var Convenience_Average_Error=<?php echo e($results[0]->ep_convenienceError); ?>;
                                      document.write(Convenience_Average_Error.toFixed(2));
                                    </script>
                                    %
                                  </h5>
                                </td>
                              </tr>
                              <tr>
                                <td><h5>Budget Average Error:</h5></td>
                                <td><h5><?php echo e($results[0]->ep_budgetError); ?> %</h5></td>
                              </tr>                              
                              <tr>
                                <td><h5>Standard Deviation:</h5></td>
                                <td>
                                  <h5>
                                    <script>
                                      var Standard_Deviation=<?php echo e($results[0]->ep_standardDeviation); ?>;
                                      document.write(Standard_Deviation.toFixed(2));
                                    </script>
                                    %
                                  </h5>
                                </td>
                              </tr>

                              <?php $__currentLoopData = $users; $__env->addLoop($__currentLoopData); foreach($__currentLoopData as $user): $__env->incrementLoopIndices(); $loop = $__env->getLastLoop(); ?>
                              <tr>
                                <td>
                                  <h5>Convenience Average Error for user: <?php echo e($user->name); ?></h5>
                                </td>
                                <td>
                                  <h5>
                                    <script>
                                      var usersConvenienceError=<?php echo e($results[0]->ep_convenienceError); ?>*<?php echo e($user->rulesCounter); ?>/<?php echo e($totalNumRules[0]->allRulesCounter); ?>;
                                      document.write(usersConvenienceError.toFixed(2));
                                    </script>
                                    %
                                  </h5>
                                </td>
                              </tr>
                              <?php endforeach; $__env->popLoop(); $loop = $__env->getLastLoop(); ?>
                              
                          </table>

                        

                      </div>
                  </div>
              </div>
          </div>
      </div>
  </div>

  <script type="text/javascript">
      google.charts.load('current', {'packages':['gauge']});
      google.charts.load('current', {'packages':['corechart']});
      google.charts.setOnLoadCallback(drawChart);
      google.charts.setOnLoadCallback(drawChart2);

      function drawChart() {

        var kwh_consumed=<?php echo e($results[0]->ep_kWh); ?>;
        var kwh_limit=<?php echo e($kWhLimit[0]->value); ?>;
        var kwhPercentage= (100*kwh_consumed/kwh_limit).toFixed(2);
        //alert(kwh_percentage);
        
        var data = google.visualization.arrayToDataTable([
          ['Label', 'Value'],
          ['Energy', parseFloat(kwhPercentage)],
          ['Convenience', (100-<?php echo e($results[0]->ep_convenienceError); ?>).toFixed(2)],
          ['Budget', (100-<?php echo e($results[0]->ep_budgetError); ?>).toFixed(2)]
        ]);

        var options = {
          //width: 100%, height: 100%,
          greenFrom: 70, greenTo: 100,
      redFrom: 0, redTo: 20,
          yellowFrom:50, yellowTo: 70,
          minorTicks: 5
        };

        var chart = new google.visualization.Gauge(document.getElementById('chart_div'));

        chart.draw(data, options);
       
      }

      function drawChart2() {

        var data = google.visualization.arrayToDataTable([
          ['Kilowatt hour (kWh)', 'kWh Budget Threshold', 'kWh Consumed'],
          ['',  <?php echo e($kWhLimit[0]->value); ?>,      <?php echo e($results[0]->ep_kWh); ?>],
          ['',  <?php echo e($kWhLimit[0]->value); ?>,      <?php echo e($results[0]->ep_kWh); ?>],
          ['',  <?php echo e($kWhLimit[0]->value); ?>,       <?php echo e($results[0]->ep_kWh); ?>],
          ['',  <?php echo e($kWhLimit[0]->value); ?>,      <?php echo e($results[0]->ep_kWh); ?>]
        ]);

        var options = {
          title: 'Breakdown of the Energy Consumption (kWh)',
          hAxis: {title: 'Kilowatt hour (kWh)',  titleTextStyle: {color: '#333'}},
          vAxis: {minValue: 0},
          backgroundColor: ''
        };

        var chart = new google.visualization.AreaChart(document.getElementById('chart_div2'));
        chart.draw(data, options);
       
      }
    </script>


  <?php $__env->stopSection(); ?>

<?php echo $__env->make('layouts.app', \Illuminate\Support\Arr::except(get_defined_vars(), ['__data', '__path']))->render(); ?><?php /**PATH /var/www/html/imcf/resources/views/results/index.blade.php ENDPATH**/ ?>