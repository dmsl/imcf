

  <?php $__env->startSection('content'); ?>
  <div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
            <a href="<?php echo e(URL::previous()); ?>" class="btn btn-primary">Go Back</a>
            <br><br>

              <div class="card">
                  <div class="card-header">OpenHAB Configurations - Cloud Controller</div>

                  <div class="card-body">
                      <div class="panel-body">
                        <div>
                          <div class="float-md-left"><h1>OpenHAB Rules Table</h1> </div>
                          <div class="float-md-right"><a href="/retrieve-openhab-cloud" class="btn btn-primary">Retrieve OpenHAB Rules</a></div>
                        </div>

                        <?php if(count($openhabRules)>0): ?>
                        <table class="table table-striped">
                            <tr>
                              <th>Description</th>
                              <th>Type</th>
                              <th>State</th>
                              <th>Mode</th>
                            </tr>
                            <?php $__currentLoopData = $openhabRules; $__env->addLoop($__currentLoopData); foreach($__currentLoopData as $openhabRule): $__env->incrementLoopIndices(); $loop = $__env->getLastLoop(); ?>
                              <?php if($openhabRule->type != 'Group'): ?>
                                <tr>
                                  <td> <!--- <?php echo e($openhabRule->name); ?> --->
                                    <script>
                                      document.write("<?php echo e($openhabRule->label); ?>".replace(/_/g, ' '));
                                    </script>
                                  </td>
                                  <td>                                  
                                      <?php echo e($openhabRule->category); ?> 
                                  </td>
                                  <td>
                                    <!---<?php if($openhabRule->state == 'ON'): ?>
                                      <button type="button" class="btn btn-success">ON</button>
                                    <?php elseif($openhabRule->state == 'OFF'): ?>
                                      <button type="button" class="btn btn-danger">OFF</button>
                                    <?php elseif($openhabRule->type == 'DateTime'): ?>
                                      NULL
                                    <?php elseif(($openhabRule->name == 'ImageURL') ||  ($openhabRule->type == 'Location')): ?>
                                        NULL
                                    <?php else: ?>
                                      <?php echo e($openhabRule->state); ?>

                                    <?php endif; ?> --->
                                    <?php echo e($openhabRule->state); ?>

                                  </td>
                                  <td>
                                    <?php if($openhabRule->stateONOFF == 'ON'): ?>
                                      <button type="button" class="btn btn-success">ON</button>
                                    <?php elseif($openhabRule->stateONOFF == 'OFF'): ?>
                                      <button type="button" class="btn btn-danger">OFF</button>
                                    <?php endif; ?>
                                  </td>
                                </tr>
                              <?php endif; ?> 
                            <?php endforeach; $__env->popLoop(); $loop = $__env->getLastLoop(); ?>
                        </table>
                        <?php endif; ?>

                      </div>
                  </div>
              </div>
          </div>
      </div>
  </div>


<script>

setTimeout(function(){
   window.location.reload(1);
}, 3000);

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


  <?php $__env->stopSection(); ?>

<?php echo $__env->make('layouts.app', \Illuminate\Support\Arr::except(get_defined_vars(), ['__data', '__path']))->render(); ?><?php /**PATH /home/vagrant/Laravel/imcf/resources/views/openhab/openhabcloud.blade.php ENDPATH**/ ?>