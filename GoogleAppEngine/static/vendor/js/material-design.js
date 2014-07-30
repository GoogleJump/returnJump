/*!
 * Angular Material Design
 * WIP Banner
 */
(function(){
angular.module('ngMaterial', [ 'ng', 'ngAnimate', 'material.services', "material.components.backdrop","material.components.button","material.components.card","material.components.checkbox","material.components.content","material.components.dialog","material.components.form","material.components.icon","material.components.list","material.components.radioButton","material.components.scrollHeader","material.components.sidenav","material.components.slider","material.components.tabs","material.components.toast","material.components.toolbar","material.components.whiteframe"]);
angular.module('material.animations', ['ngAnimateStylers', 'ngAnimateSequence', 'ngAnimate', 'material.services'])
       .service('materialEffects', [ '$animateSequence', '$ripple', '$rootElement', '$position', '$$rAF', MaterialEffects])
       .directive('materialRipple', ['materialEffects', '$interpolate', '$throttle', MaterialRippleDirective]);

/**
 * This service provides animation features for various Material Design effects:
 *
 *  1) ink stretchBars,
 *  2) ink ripples,
 *  3) popIn animations
 *  4) popOuts animations
 *
 * @constructor
 */
function MaterialEffects($animateSequence, $ripple, $rootElement, $position, $$rAF) {

  var styler = angular.isDefined( $rootElement[0].animate ) ? 'webAnimations' :
               angular.isDefined( window['TweenMax'] || window['TweenLite'] ) ? 'gsap'   :
               angular.isDefined( window['jQuery'] ) ? 'jQuery' : 'default';

  // Publish API for effects...

  return {
    inkRipple: animateInkRipple,
    inkBar: animateInkBar,
    popIn: popIn,
    popOut: popOut
  };

  // **********************************************************
  // API Methods
  // **********************************************************

  /**
   * Use the canvas animator to render the ripple effect(s).
   */
  function animateInkRipple( canvas, options )
  {
    return new $ripple(canvas, options);
  }


  /**
   * Make instance of a reusable sequence and
   * auto-run the sequence on the element (if defined)
   */
  function animateInkBar(element, styles, duration ) {
    var animate = $animateSequence({ styler: styler }).animate,
      sequence = animate( {}, styles, safeDuration(duration || 350) );

    return angular.isDefined(element) ? sequence.run(element) : sequence;
  }


  /**
   *
   */
  function popIn(element, parentElement, clickElement) {
    var startPos;
    var endPos = $position.positionElements(parentElement, element, 'center');
    if (clickElement) {
      var dialogPos = $position.position(element);
      var clickPos = $position.offset(clickElement);
      startPos = {
        left: clickPos.left - dialogPos.width / 2,
        top: clickPos.top - dialogPos.height / 2
      };
    } else {
      startPos = endPos;
    }

    // TODO once ngAnimateSequence bugs are fixed, this can be switched to use that
    element.css({
      '-webkit-transform': translateString(startPos.left, startPos.top, 0) + ' scale(0.2)',
      opacity: 0
    });
    $$rAF(function() {
      element.addClass('dialog-changing');
      $$rAF(function() {
        element.css({
          '-webkit-transform': translateString(endPos.left, endPos.top, 0) + ' scale(1.0)',
          opacity: 1
        });
      });
    });
  }

  /**
   *
   *
   */
  function popOut(element, parentElement) {
    var endPos = $position.positionElements(parentElement, element, 'bottom-center');

    endPos.top -= element.prop('offsetHeight') / 2;

    var runner = $animateSequence({ styler: styler })
      .addClass('dialog-changing')
      .then(function() {
        element.css({
          '-webkit-transform': translateString(endPos.left, endPos.top, 0) + ' scale(0.5)',
          opacity: 0
        });
      });

    return runner.run(element);
  }


  // **********************************************************
  // Utility Methods
  // **********************************************************


  function translateString(x, y, z) {
    return 'translate3d(' + x + 'px,' + y + 'px,' + z + 'px)';
  }


  /**
   * Support values such as 0.65 secs or 650 msecs
   */
  function safeDuration(value) {
    var duration = isNaN(value) ? 0 : Number(value);
    return (duration < 1.0) ? (duration * 1000) : duration;
  }

  /**
   * Convert all values to decimal;
   * eg 150 msecs -> 0.15sec
   */
  function safeVelocity(value) {
    var duration = isNaN(value) ? 0 : Number(value);
    return (duration > 100) ? (duration / 1000) :
      (duration > 10 ) ? (duration / 100) :
        (duration > 1  ) ? (duration / 10) : duration;
  }

}

/**
 *  <material-ripple /> Directive
 */
function MaterialRippleDirective(materialEffects, $interpolate, $throttle) {
  return {
    restrict: 'E',
    compile: compileWithCanvas
  };

  /**
   * Use Javascript and Canvas to render ripple effects
   *
   * Note: attribute start="" has two (2) options: `center` || `pointer`; which
   * defines the start of the ripple center.
   *
   * @param element
   * @returns {Function}
   */
  function compileWithCanvas( element, attrs ) {
    var RIGHT_BUTTON = 2;
    var options  = calculateOptions(element, attrs);
    var tag =
      '<canvas ' +
        'class="material-ink-ripple {{classList}}"' +
        'style="top:{{top}}; left:{{left}}" >' +
      '</canvas>';

    element.replaceWith(
      angular.element( $interpolate(tag)(options) )
    );

    return function linkCanvas( scope, element ){

      var ripple, watchMouse,
          parent = element.parent(),
          makeRipple = $throttle({
            start : function() {
              ripple = ripple || materialEffects.inkRipple( element[0], options );
              watchMouse = watchMouse || buildMouseWatcher(parent, makeRipple);

              // Ripples start with left mouseDowns (or taps)
              parent.on('mousedown', makeRipple);
            },
            throttle : function(e, done) {
              if ( effectAllowed() )
              {
                switch(e.type)
                {
                  case 'mousedown' :
                    // If not right- or ctrl-click...
                    if (!e.ctrlKey && (e.button !== RIGHT_BUTTON))
                    {
                      watchMouse(true);
                      ripple.createAt( options.forceToCenter ? null : localToCanvas(e) );
                    }
                    break;

                  default:
                    watchMouse(false);

                    // Draw of each wave/ripple in the ink only occurs
                    // on mouseup/mouseleave

                    ripple.draw( done );
                    break;
                }
              } else {
                done();
              }
            },
            end : function() {
              watchMouse(false);
            }
          })();


      // **********************************************************
      // Utility Methods
      // **********************************************************

      /**
       * If the ripple canvas been removed from the DOM, then
       * remove the `mousedown` listener
       *
       * @returns {*|boolean}
       */
      function effectAllowed() {
        var allowed = isInkEnabled( element.scope() ) && angular.isDefined( element.parent()[0] );
        if ( !allowed ) {
          parent.off('mousedown', makeRipple);
        }
        return allowed;


        /**
         * Check scope chain for `inkEnabled` or `disabled` flags...
         */
        function isInkEnabled(scope) {
          return angular.isUndefined(scope) ? true :
            angular.isDefined(scope.disabled) ? !scope.disabled :
              angular.isDefined(scope.inkEnabled) ? scope.inkEnabled : true;
        }

      }

      /**
       * Build mouse event listeners for the specified element
       * @param element Angular element that will listen for bubbling mouseEvents
       * @param handlerFn Function to be invoked with the mouse event
       * @returns {Function}
       */
      function buildMouseWatcher(element, handlerFn) {
        // Return function to easily toggle on/off listeners
        return function watchMouse(active) {
          angular.forEach("mouseup,mouseleave".split(","), function(eventType) {
            var fn = active ? element.on : element.off;
            fn.apply(element, [eventType, handlerFn]);
          });
        }
      }
      /**
       * Convert the mouse down coordinates from `parent` relative
       * to `canvas` relative; needed since the event listener is on
       * the parent [e.g. tab element]
       */
      function localToCanvas(e)
      {
        var canvas = element[0].getBoundingClientRect();

        return  {
          x : e.clientX - canvas.left,
          y : e.clientY - canvas.top
        };
      }

    }

    function calculateOptions(element, attrs)
    {
      return angular.extend( getBounds(element), {
        classList : (attrs.class || ""),
        forceToCenter : (attrs.start == "center"),
        initialOpacity : getFloatValue( attrs, "initialOpacity" ),
        opacityDecayVelocity : getFloatValue( attrs, "opacityDecayVelocity" )
      });

      function getBounds(element) {
        var node = element[0];
        var styles  =  node.ownerDocument.defaultView.getComputedStyle( node, null ) || { };

        return  {
          left : (styles.left == "auto" || !styles.left) ? "0px" : styles.left,
          top : (styles.top == "auto" || !styles.top) ? "0px" : styles.top,
          width : getValue( styles, "width" ),
          height : getValue( styles, "height" )
        };
      }

      function getFloatValue( map, key, defaultVal )
      {
        return angular.isDefined( map[key] ) ? +map[key] : defaultVal;
      }

      function getValue( map, key, defaultVal )
      {
        var val = map[key];
        return (angular.isDefined( val ) && (val !== ""))  ? map[key] : defaultVal;
      }
    }

  }



}

angular.module('material.animations')
/**
 * Port of the Polymer Paper-Ripple code
 * This service returns a reference to the Ripple class
 *
 * @group Paper Elements
 * @element paper-ripple
 * @homepage github.io
 */
  .service('$ripple', ['$$rAF', function($$rAF) {

    /**
     * Unlike angular.extend() will always overwrites destination,
     * mixin() only overwrites the destination if it is undefined; so
     * pre-existing destination values are **not** modified.
     */
    var mixin = function (dst) {
      angular.forEach(arguments, function(obj) {
        if (obj !== dst) {
          angular.forEach(obj, function(value, key) {
            // Only mixin if destination value is undefined
            if ( angular.isUndefined(dst[key]) )
            {
              dst[key] = value;
            }
          });
        }
      });
      return dst;
    };

    // **********************************************************
    // Ripple Class
    // **********************************************************

    return (function(){

      var now = Date.now;

      if (window.performance && performance.now) {
        now = performance.now.bind(performance);
      }

      /**
       *  Ripple creates a `paper-ripple` which is a visual effect that other quantum paper elements can
       *  use to simulate a rippling effect emanating from the point of contact.  The
       *  effect can be visualized as a concentric circle with motion.
       */
      function Ripple(canvas, options) {

        var defaults = {
          onComplete: angular.noop,   // Completion hander/callback
          initialOpacity: 0.6,       // The initial default opacity set on the wave.
          opacityDecayVelocity: 1.7, // How fast (opacity per second) the wave fades out.
          backgroundFill: true,
          pixelDensity: 1
        };

        this.canvas = canvas;
        this.waves = [];
        this.cAF = undefined;

        return angular.extend(this, mixin(options || { }, defaults));
      }

      /**
       *  Define class methods
       */
      Ripple.prototype = {

        /**
         *
         */
        createAt : function (startAt) {
          var canvas = this.adjustBounds(this.canvas);
          var wave = createWave(canvas);

          var width = canvas.width / this.pixelDensity;
          var height = canvas.height / this.pixelDensity;

          // Auto center ripple if startAt is not defined...
          startAt = startAt || { x: Math.round(width / 2), y: Math.round(height / 2) };

          wave.isMouseDown = true;
          wave.mouseDownStart = now();
          wave.mouseUpStart = 0.0;
          wave.tDown = 0.0;
          wave.tUp = 0.0;
          wave.startPosition = startAt;
          wave.containerSize = Math.max(width, height);
          wave.maxRadius = distanceFromPointToFurthestCorner(wave.startPosition, {w: width, h: height}) * 0.75;

          if (this.canvas.classList.contains("recenteringTouch")) {
            wave.endPosition = {x: width / 2, y: height / 2};
            wave.slideDistance = dist(wave.startPosition, wave.endPosition);
          }

          this.waves.push(wave);
          this.cancelled = false;

          this.animate();
        },

        /**
         *
         */
        draw : function (done) {
          this.onComplete = done;

          for (var i = 0; i < this.waves.length; i++) {
            // Declare the next wave that has mouse down to be mouse'ed up.
            var wave = this.waves[i];
            if (wave.isMouseDown) {
              wave.isMouseDown = false
              wave.mouseDownStart = 0;
              wave.tUp = 0.0;
              wave.mouseUpStart = now();
              break;
            }
          }
          this.animate();
        },

        /**
         *
         */
        cancel : function () {
          this.cancelled = true;
          return this;
        },

        /**
         *  Stop or start rendering waves for the next animation frame
         */
        animate : function (active) {
          if (angular.isUndefined(active)) active = true;

          if (active === false) {
            if (angular.isDefined(this.cAF)) {
              this._loop = null;
              this.cAF();

              // Notify listeners [via callback] of animation completion
              this.onComplete();
            }
          } else {
            if (!this._loop) {
              this._loop = angular.bind(this, function () {
                var ctx = this.canvas.getContext('2d');
                ctx.scale(this.pixelDensity, this.pixelDensity);

                this.onAnimateFrame(ctx);
              });
            }
            this.cAF = $$rAF(this._loop);
          }
        },

        /**
         *
         */
        onAnimateFrame : function (ctx) {
          // Clear the canvas
          ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

          var deleteTheseWaves = [];
          // wave animation values
          var anim = {
            initialOpacity: this.initialOpacity,
            opacityDecayVelocity: this.opacityDecayVelocity,
            height: ctx.canvas.height,
            width: ctx.canvas.width
          };

          for (var i = 0; i < this.waves.length; i++) {
            var wave = this.waves[i];

            if ( !this.cancelled ) {

              if (wave.mouseDownStart > 0) {
                wave.tDown =  now() - wave.mouseDownStart;
              }
              if (wave.mouseUpStart > 0) {
                wave.tUp = now() - wave.mouseUpStart;
              }

              // Obtain the instantaneous size and alpha of the ripple.
              // Determine whether there is any more rendering to be done.

              var radius = waveRadiusFn(wave.tDown, wave.tUp, anim);
              var maximumWave = waveAtMaximum(wave, radius, anim);
              var waveDissipated = waveDidFinish(wave, radius, anim);
              var shouldKeepWave = !waveDissipated || !maximumWave;

              if (!shouldKeepWave) {

                deleteTheseWaves.push(wave);

              } else {


                drawWave( wave, angular.extend( anim, {
                  radius : radius,
                  backgroundFill : this.backgroundFill,
                  ctx : ctx
                }));

              }
            }
          }

          if ( this.cancelled ) {
            // Clear all waves...
            deleteTheseWaves = deleteTheseWaves.concat( this.waves );
          }
          for (var i = 0; i < deleteTheseWaves.length; ++i) {
            removeWave( deleteTheseWaves[i], this.waves );
          }

          if (!this.waves.length) {
            // If there is nothing to draw, clear any drawn waves now because
            // we're not going to get another requestAnimationFrame any more.
            ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

            // stop animations
            this.animate(false);

          } else if (!waveDissipated && !maximumWave) {
            this.animate();
          }

          return this;
        },

        /**
         *
         */
        adjustBounds : function (canvas) {
          // Default to parent container to define bounds
          var self = this,
            src = canvas.parentNode.getBoundingClientRect(),  // read-only
            bounds = { width: src.width, height: src.height };

          angular.forEach("width height".split(" "), function (style) {
            var value = (self[style] != "auto") ? self[style] : undefined;

            // Allow CSS to explicitly define bounds (instead of parent container
            if (angular.isDefined(value)) {
              bounds[style] = sanitizePosition(value);
              canvas.setAttribute(style, bounds[style] * self.pixelDensity + "px");
            }

          });

          // NOTE: Modified from polymer implementation
          canvas.setAttribute('width', bounds.width * this.pixelDensity + "px");
          canvas.setAttribute('height', bounds.height * this.pixelDensity + "px");

          function sanitizePosition(style) {
            var val = style.replace('px', '');
            return val;
          }

          return canvas;
        }

      };

      // Return class reference

      return Ripple;
    })();




    // **********************************************************
    // Private Wave Methods
    // **********************************************************

    /**
     *
     */
    function waveRadiusFn(touchDownMs, touchUpMs, anim) {
      // Convert from ms to s.
      var waveMaxRadius = 150;
      var touchDown = touchDownMs / 1000;
      var touchUp = touchUpMs / 1000;
      var totalElapsed = touchDown + touchUp;
      var ww = anim.width, hh = anim.height;
      // use diagonal size of container to avoid floating point math sadness
      var waveRadius = Math.min(Math.sqrt(ww * ww + hh * hh), waveMaxRadius) * 1.1 + 5;
      var duration = 1.1 - .2 * (waveRadius / waveMaxRadius);
      var tt = (totalElapsed / duration);

      var size = waveRadius * (1 - Math.pow(80, -tt));
      return Math.abs(size);
    }

    /**
     *
     */
    function waveOpacityFn(td, tu, anim) {
      // Convert from ms to s.
      var touchDown = td / 1000;
      var touchUp = tu / 1000;

      return (tu <= 0) ? anim.initialOpacity : Math.max(0, anim.initialOpacity - touchUp * anim.opacityDecayVelocity);
    }

    /**
     *
     */
    function waveOuterOpacityFn(td, tu, anim) {
      // Convert from ms to s.
      var touchDown = td / 1000;
      var touchUp = tu / 1000;

      // Linear increase in background opacity, capped at the opacity
      // of the wavefront (waveOpacity).
      var outerOpacity = touchDown * 0.3;
      var waveOpacity = waveOpacityFn(td, tu, anim);
      return Math.max(0, Math.min(outerOpacity, waveOpacity));
    }


    /**
     * Determines whether the wave should be completely removed.
     */
    function waveDidFinish(wave, radius, anim) {
      var waveMaxRadius = 150;
      var waveOpacity = waveOpacityFn(wave.tDown, wave.tUp, anim);
      // If the wave opacity is 0 and the radius exceeds the bounds
      // of the element, then this is finished.
      if (waveOpacity < 0.01 && radius >= Math.min(wave.maxRadius, waveMaxRadius)) {
        return true;
      }
      return false;
    };

    /**
     *
     */
    function waveAtMaximum(wave, radius, anim) {
      var waveMaxRadius = 150;
      var waveOpacity = waveOpacityFn(wave.tDown, wave.tUp, anim);
      if (waveOpacity >= anim.initialOpacity && radius >= Math.min(wave.maxRadius, waveMaxRadius)) {
        return true;
      }
      return false;
    }

    /**
     *
     */
    function createWave(elem) {
      var elementStyle = window.getComputedStyle(elem);

      var wave = {
        waveColor: elementStyle.color,
        maxRadius: 0,
        isMouseDown: false,
        mouseDownStart: 0.0,
        mouseUpStart: 0.0,
        tDown: 0,
        tUp: 0
      };
      return wave;
    }

    /**
     *
     */
    function removeWave(wave, buffer) {
      if (buffer && buffer.length) {
        var pos = buffer.indexOf(wave);
        buffer.splice(pos, 1);
      }
    }

    function drawWave ( wave, anim ) {

      // Calculate waveColor and alphas; if we do a background
      // fill fade too, work out the correct color.

      anim.waveColor = cssColorWithAlpha(
        wave.waveColor,
        waveOpacityFn(wave.tDown, wave.tUp, anim)
      );

      if ( anim.backgroundFill ) {
        anim.backgroundFill = cssColorWithAlpha(
          wave.waveColor,
          waveOuterOpacityFn(wave.tDown, wave.tUp, anim)
        );
      }

      // Position of the ripple.
      var x = wave.startPosition.x;
      var y = wave.startPosition.y;

      // Ripple gravitational pull to the center of the canvas.
      if ( wave.endPosition ) {

        // This translates from the origin to the center of the view  based on the max dimension of
        var translateFraction = Math.min(1, anim.radius / wave.containerSize * 2 / Math.sqrt(2));

        x += translateFraction * (wave.endPosition.x - wave.startPosition.x);
        y += translateFraction * (wave.endPosition.y - wave.startPosition.y);
      }

      // Draw the ripple.
      renderRipple(anim.ctx, x, y, anim.radius, anim.waveColor, anim.backgroundFill);

      // Render the ripple on the target canvas 2-D context
      function renderRipple(ctx, x, y, radius, innerColor, outerColor) {
        if (outerColor) {
          ctx.fillStyle = outerColor || 'rgba(252, 252, 158, 1.0)';
          ctx.fillRect(0,0,ctx.canvas.width, ctx.canvas.height);
        }
        ctx.beginPath();

        ctx.arc(x, y, radius, 0, 2 * Math.PI, false);
        ctx.fillStyle = innerColor || 'rgba(252, 252, 158, 1.0)';
        ctx.fill();

        ctx.closePath();
      }
    }


    /**
     *
     */
    function cssColorWithAlpha(cssColor, alpha) {
      var parts = cssColor ? cssColor.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/) : null;
      if (typeof alpha == 'undefined') {
        alpha = 1;
      }
      if (!parts) {
        return 'rgba(255, 255, 255, ' + alpha + ')';
      }
      return 'rgba(' + parts[1] + ', ' + parts[2] + ', ' + parts[3] + ', ' + alpha + ')';
    }

    /**
     *
     */
    function dist(p1, p2) {
      return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     *
     */
    function distanceFromPointToFurthestCorner(point, size) {
      var tl_d = dist(point, {x: 0, y: 0});
      var tr_d = dist(point, {x: size.w, y: 0});
      var bl_d = dist(point, {x: 0, y: size.h});
      var br_d = dist(point, {x: size.w, y: size.h});
      return Math.max(tl_d, tr_d, bl_d, br_d);
    }

  }]);






angular.module('material.components.backdrop', [])

.service('$materialBackdrop', [
  '$materialPopup',
  '$timeout',
  '$rootElement',
  MaterialBackdropService
]);

function MaterialBackdropService($materialPopup, $timeout, $rootElement) {

  return showBackdrop;

  function showBackdrop(options, clickFn) {
    var appendTo = options.appendTo || $rootElement;
    var opaque = options.opaque;

    return $materialPopup({
      template: '<material-backdrop class="ng-enter">',
      appendTo: options.appendTo
    }).then(function(backdrop) {
      clickFn && backdrop.element.on('click', function(ev) {
        $timeout(function() {
          clickFn(ev);
        });
      });
      opaque && backdrop.element.addClass('opaque');

      return backdrop;
    });
  }
}

/**
 * @ngdoc overview
 * @name material.components.button
 *
 * @description
 * Button components.
 */
angular.module('material.components.button', []);

/**
 * @ngdoc overview
 * @name material.components.card
 *
 * @description
 * Card components.
 */
angular.module('material.components.card', [])
  .directive('materialCard', [ materialCardDirective ]);

function materialCardDirective() {
  return {
    restrict: 'E',
    link: function($scope, $element, $attr) {
    }
  }
}

/**
 * @ngdoc module
 * @name material.components.checkbox
 * @description Checkbox module!
 */
angular.module('material.components.checkbox', [])
  .directive('materialCheckbox', [ materialCheckboxDirective ]);

/**
 * @ngdoc directive
 * @name materialCheckbox
 * @module material.components.checkbox
 * @restrict E
 *
 * @description
 * Checkbox directive!
 *
 * @param {expression=} ngModel An expression to bind this checkbox to.
 */
function materialCheckboxDirective() {

  var CHECKED_CSS = 'material-checked';

  return {
    restrict: 'E',
    scope: true,
    transclude: true,
    template: '<div class="material-container">' +
                '<material-ripple start="center" class="circle" material-checked="{{ checked }}" ></material-ripple>' +
                '<div class="material-icon"></div>' +
              '</div>' +
              '<div ng-transclude class="material-label"></div>',
    link: link
  };

  // **********************************************************
  // Private Methods
  // **********************************************************

  function link(scope, element, attr) {
    var input = element.find('input');
    var ngModelCtrl = angular.element(input).controller('ngModel');
    scope.checked = false;

    if(!ngModelCtrl || input[0].type !== 'checkbox') return;

    // watch the ng-model $viewValue
    scope.$watch(
      function () { return ngModelCtrl.$viewValue; },
      function () {
        scope.checked = input[0].checked;

        element.attr('aria-checked', scope.checked);
        if(scope.checked) {
          element.addClass(CHECKED_CSS);
        } else {
          element.removeClass(CHECKED_CSS);
        }
      }
    );

    // add click listener to directive element to manually
    // check the inner input[checkbox] and set $viewValue
    var listener = function(ev) {
      scope.$apply(function() {
        input[0].checked = !input[0].checked;
        ngModelCtrl.$setViewValue(input[0].checked, ev && ev.type);
      });
    };
    element.on('click', listener);

  }

}



/**
 * @ngdoc overview
 * @name material.components.content
 *
 * @description
 * Scrollable content
 */
angular.module('material.components.content', [
  'material.services.registry'
])

.controller('$materialContentController', ['$scope', '$element', '$attrs', '$materialComponentRegistry', materialContentController])
.factory('$materialContent', ['$materialComponentRegistry', materialContentService])
.directive('materialContent', [materialContentDirective])

function materialContentController($scope, $element, $attrs, $materialComponentRegistry) {
  $materialComponentRegistry.register(this, $attrs.componentId || 'content');

  this.getElement = function() {
    return $element;
  };
}

function materialContentService($materialComponentRegistry) {
  return function(handle) {
    var instance = $materialComponentRegistry.get(handle);
    if(!instance) {
      $materialComponentRegistry.notFoundError(handle);
    }
    return instance;
  };
}


function materialContentDirective() {
  return {
    restrict: 'E',
    transclude: true,
    template: '<div class="material-content" ng-transclude></div>',
    controller: '$materialContentController',
    link: function($scope, $element, $attr) {
    }
  }
}

angular.module('material.components.dialog', ['material.services.popup'])
  .directive('materialDialog', [
    MaterialDialogDirective
  ])
  /**
   * @ngdoc service
   * @name $materialDialog
   * @module material.components.dialog
   */
  .factory('$materialDialog', [
    '$timeout',
    '$materialPopup',
    '$rootElement',
    '$materialBackdrop',
    'materialEffects',
    MaterialDialogService
  ]);

function MaterialDialogDirective() {
  return {
    restrict: 'E'
  };
}

function MaterialDialogService($timeout, $materialPopup, $rootElement, $materialBackdrop, materialEffects) {
  var recentDialog;

  return showDialog;

  /**
   * TODO fully document this
   * Supports all options from $materialPopup, in addition to `duration` and `position`
   */
  function showDialog(options) {
    options = angular.extend({
      appendTo: $rootElement,
      hasBackdrop: true, // should have an opaque backdrop
      clickOutsideToClose: true, // should have a clickable backdrop to close
      escapeToClose: true,
      // targetEvent: used to find the location to start the dialog from
      targetEvent: null
      // Also supports all options from $materialPopup
    }, options || {});

    var backdropInstance;

    // Close the old dialog
    recentDialog && recentDialog.then(function(destroyDialog) {
      destroyDialog();
    });

    recentDialog = $materialPopup(options).then(function(dialog) {

      // Controller will be passed a `$hideDialog` function
      dialog.locals.$hideDialog = destroyDialog;
      dialog.enter(function() {
        if (options.escapeToClose) {
          $rootElement.on('keyup', onRootElementKeyup);
        }
        if (options.hasBackdrop || options.clickOutsideToClose) {
          backdropInstance = $materialBackdrop({
            appendTo: options.appendTo,
            opaque: options.hasBackdrop
          }, clickOutsideToClose ? destroyDialog : angular.noop);
          backdropInstance.then(function(drop) {
            drop.enter();
          });
        }
      });

      materialEffects.popIn(
        dialog.element,
        options.appendTo,
        options.targetEvent && options.targetEvent.target && 
          angular.element(options.targetEvent.target)
      );

      return destroyDialog;

      function destroyDialog() {
        if (backdropInstance) {
          backdropInstance.then(function(drop) {
            drop.destroy();
          });
        }
        if (options.escapeToClose) {
          $rootElement.off('keyup', onRootElementKeyup);
        }
        materialEffects.popOut(dialog.element, $rootElement);

        // TODO once the done method from the popOut function & ngAnimateStyler works,
        // remove this timeout
        $timeout(dialog.destroy, 200);
      }
      function onRootElementKeyup(e) {
        if (e.keyCode == 27) {
          $timeout(destroyDialog);
        }
      }
    });

    return recentDialog;
  }
}

angular.module('material.components.form', [])

.directive('materialInputGroup', [materialInputGroupDirective]);

function materialInputGroupDirective() {
  return {
    restrict: 'C',
    link: function($scope, $element, $attr) {
      // Grab the input child, and just do nothing if there is no child
      var input = $element[0].querySelector('input');
      if(!input) { return; }

      input = angular.element(input);
      var ngModelCtrl = input.controller('ngModel');

      // When the input value changes, check if it "has" a value, and 
      // set the appropriate class on the input group
      if (ngModelCtrl) {
        $scope.$watch(
          function() { return ngModelCtrl.$viewValue; },
          onInputChange
        );
      }
      input.on('input', onInputChange);

      // When the input focuses, add the focused class to the group
      input.on('focus', function(e) {
        $element.addClass('material-input-focused');
      });
      // When the input blurs, remove the focused class from the group
      input.on('blur', function(e) {
        $element.removeClass('material-input-focused');
      });

      function onInputChange() {
        $element.toggleClass('material-input-has-value', !!input.val());
      }
    }
  };
}

angular.module('material.components.icon', [])
  .directive('materialIcon', [ materialIconDirective ]);

function materialIconDirective() {
  return {
    restrict: 'E',
    template: '<object class="material-icon"></object>',
    compile: function(element, attr) {
      var object = angular.element(element[0].children[0]);
      if(angular.isDefined(attr.icon)) {
        object.attr('data', attr.icon);
      }
    }
  };
}

angular.module('material.components.list', [])

.directive('materialList', [materialListDirective])
.directive('materialItem', [materialItemDirective]);

/**
 * @ngdoc directive
 * @name material.components.list.directive:material-list
 * @restrict E
 *
 * @description
 * materialList is a list container for material-items
 * @example
 * <material-list>
    <material-item>
      <div class="material-tile-left">
      </div>
      <div class="material-tile-content">
        <h2>Title</h2>
        <h3>Subtitle</h3>
        <p>
          Content
        </p>
      </div>
      <div class="material-tile-right">
      </div>
    </material-item>
 * </material-list>
 */
function materialListDirective() {
  return {
    restrict: 'E',
    link: function($scope, $element, $attr) {
    }
  }
}

/**
 * @ngdoc directive
 * @name material.components.list.directive:material-item
 * @restrict E
 *
 * @description
 * materialItem is a list item
 */
function materialItemDirective() {
  return {
    restrict: 'E',
    link: function($scope, $element, $attr) {
    }
  }
}


/**
 * @ngdoc module
 * @name material.components.radioButton
 * @description radioButton module!
 */
angular.module('material.components.radioButton', [])
  .directive('materialRadioButton', [ materialRadioButtonDirective ])
  .directive('materialRadioGroup', [ materialRadioGroupDirective ]);


/**
 * @ngdoc directive
 * @name materialRadioButton
 * @module material.components.radioButton
 * @restrict E
 *
 * @description
 * radioButton directive!
 *
 * @param {expression=} ngModel An expression to bind this radioButton to.
 */
function materialRadioButtonDirective() {

  var CHECKED_CSS = 'material-checked';

  return {
    restrict: 'E',
    require: '^materialRadioGroup',
    scope: true,
    transclude: true,
    template: '<div class="material-container">' +
                '<material-ripple start="center" class="circle"></material-ripple>' +
                '<div class="material-off"></div>' +
                '<div class="material-on"></div>' +
              '</div>' +
              '<div ng-transclude class="material-label"></div>',
    link: link
  };

  // **********************************************************
  // Private Methods
  // **********************************************************

  function link(scope, element, attr, rgCtrl) {
    var input = element.find('input');
    var ngModelCtrl = angular.element(input).controller('ngModel');
    scope.checked = false;

    if(!ngModelCtrl || input[0].type !== 'radio') return;

    // the radio group controller decides if this
    // radio button should be checked or not
    scope.check = function(val) {
      // update the directive's DOM/design
      scope.checked = !!val;
      element.attr('aria-checked', scope.checked);
      if(scope.checked) {
        element.addClass(CHECKED_CSS);
      } else {
        element.removeClass(CHECKED_CSS);
      }
    };

    // watch the ng-model $viewValue
    scope.$watch(
      function () { return ngModelCtrl.$viewValue; },
      function (val) {
        // tell the radio group controller that this
        // radio button should be the checked one
        if(input[0].checked) {
          rgCtrl.check(scope);
        }
      }
    );

    // add click listener to directive element to manually
    // check the inner input[radio] and set $viewValue
    var listener = function(ev) {
      scope.$apply(function() {
        ngModelCtrl.$setViewValue(input.val(), ev && ev.type);
        input[0].checked = true;
      });
    };
    element.on('click', listener);

    // register this radio button in its radio group
    rgCtrl.add(scope);

    // on destroy, remove this radio button from its radio group
    scope.$on('$destroy', function(){
      if(input[0].checked) {
        ngModelCtrl.$setViewValue(null);
      }
      rgCtrl.remove(scope);
    });
  }

}


/**
 * @ngdoc directive
 * @name radioGroup
 * @module material.components.radioGroup
 * @restrict E
 *
 * @description
 * radioGroup directive!
 */
function materialRadioGroupDirective() {

  return {
    restrict: 'E',
    controller: controller
  };

  function controller($scope) {
    var radioButtons = [];
    var checkedRadioButton = null;

    this.add = addRadioButton;
    this.remove = removeRadioButton;
    this.check = checkRadioButton;

    function addRadioButton(rbScope) {
      return radioButtons.push(rbScope);
    }

    function removeRadioButton(rbScope) {
      for(var i=0; i<radioButtons.length; i++) {
        if(radioButtons[i] === rbScope) {
          if(rbScope === checkedRadioButton) {
            checkedRadioButton = null;
          }
          return radioButtons.splice(i, 1);
        }
      }
    }

    function checkRadioButton(rbScope) {
      if(checkedRadioButton === rbScope) return;

      checkedRadioButton = rbScope;

      angular.forEach(radioButtons, function(rb) {
        rb.check(rb === checkedRadioButton);
      });
    }

  }

}

/**
 * @ngdoc overview
 * @name material.components.scrollHeader
 *
 * @description
 * Scrollable content
 */
angular.module('material.components.scrollHeader', [
  'material.components.content',
  'material.services.registry'
])

.directive('scrollHeader', [ '$materialContent', '$timeout', materialScrollHeader ]);

function materialScrollHeader($materialContent, $timeout) {

  return {
    restrict: 'A',
    link: function($scope, $element, $attr) {
      var target = $element[0],

        // Full height of the target
        height = target.offsetHeight,

        // Condensed height is set through condensedHeight or defaults to 1/3 the 
        // height of the target
        condensedHeight = $attr.condensedHeight || (height / 3),

        // Calculate the difference between the full height and the condensed height
        margin = height - condensedHeight,

        // Current "y" position of scroll
        y = 0,
      
        // Store the last scroll top position
        prevScrollTop = 0;

      // Perform a simple Y translate
      var translate = function(y) {
        target.style.webkitTransform = target.style.transform = 'translate3d(0, ' + y + 'px, 0)';
      }


      // Transform the header as we scroll
      var transform = function(y) {
        translate(-y);
      }

      // Shrink the given target element based on the scrolling
      // of the scroller element.
      var shrink = function(scroller) {
        var scrollTop = scroller.scrollTop;

        y = Math.min(height, Math.max(0, y + scrollTop - prevScrollTop));

        // If we are scrolling back "up", show the header condensed again
        if (prevScrollTop > scrollTop && scrollTop > margin) {
          y = Math.max(y, margin);
        }

        window.requestAnimationFrame(transform.bind(this, y));
      };

      // Wait for next digest to ensure content has loaded
      $timeout(function() {
        var element = $materialContent('content').getElement();

        element.on('scroll', function(e) {
          shrink(e.target);

          prevScrollTop = e.target.scrollTop;
        });
      });
    }
  };
}

/**
 * @ngdoc overview
 * @name material.components.sidenav
 *
 * @description
 * A Sidenav QP component.
 */
angular.module('material.components.sidenav', [
  'material.services.registry'
])
  .factory('$materialSidenav', [ '$materialComponentRegistry', materialSidenavService ])
  .controller('$materialSidenavController', [
      '$scope',
      '$element',
      '$attrs',
      '$timeout',
      '$document',
      '$materialSidenav',
      '$materialComponentRegistry',
    materialSidenavController ])
  .directive('materialSidenav', [ materialSidenavDirective ]);
  
/**
 * @ngdoc controller
 * @name material.components.sidenav.controller:$materialSidenavController
 *
 * @description
 * The controller for materialSidenav components.
 */
function materialSidenavController($scope, $element, $attrs, $timeout, 
    $document, $materialSidenav, $materialComponentRegistry) {

  var self = this;

  $materialComponentRegistry.register(this, $attrs.componentId);

  // Process a click event on the body to close if necessary
  var bodyClick = function(e) {
    var node = e.target;
    while(node) {
      if(node === $element[0]) {
        // Don't allow clicks originating in the sidenav to close it
        return true;
      }
      node = node.parentNode;
    }

    $scope.$apply(function() {
      self.close();
      onClose();
    });
  };
  /**
   * If the side nav is open, listen for clicks on the content to close it.
   */
  var onOpen = function() {
    $document[0].body.classList.add('material-sidenav-open');

    // Defer the event binding to avoid a false click
    $timeout(function() {
      angular.element($document[0].body).on('click', bodyClick);
    });
  };

  var onClose = function() {
    $document[0].body.classList.remove('material-sidenav-open');
    angular.element($document[0].body).off('click', bodyClick);
  };

  this.isOpen = function() {
    return !!$scope.isOpen;
  };

  /**
   * Toggle the side menu to open or close depending on its current state.
   */
  this.toggle = function() {
    $scope.isOpen = !$scope.isOpen;
    if($scope.isOpen) {
      onOpen();
    } else {
      onClose();
    }
  }

  /**
   * Open the side menu
   */
  this.open = function() {
    $scope.isOpen = true;
    onOpen();
  }

  /**
   * Close the side menu
   */
  this.close = function() {
    $scope.isOpen = false;
    onClose();
  }
}

/**
 * @ngdoc service
 * @name material.components.sidenav:$materialSidenav
 *
 * @description
 * $materialSidenav makes it easy to interact with multiple sidenavs
 * in an app.
 *
 * @usage
 *
 * ```javascript
 * // Toggle the given sidenav
 * $materialSidenav.toggle(componentId);
 * // Open the given sidenav
 * $materialSidenav.open(componentId);
 * // Close the given sidenav
 * $materialSidenav.close(componentId);
 * ```
 */
function materialSidenavService($materialComponentRegistry) {
  return function(handle) {
    var instance = $materialComponentRegistry.get(handle);
    if(!instance) {
      $materialComponentRegistry.notFoundError(handle);
    }

    return {
      isOpen: function() {
        if (!instance) { return; }
        return instance.isOpen();
      },
      /**
       * Toggle the given sidenav
       * @param handle the specific sidenav to toggle
       */
      toggle: function() {
        if(!instance) { return; }
        instance.toggle();
      },
      /**
       * Open the given sidenav
       * @param handle the specific sidenav to open
       */
      open: function(handle) {
        if(!instance) { return; }
        instance.open();
      },
      /**
       * Close the given sidenav
       * @param handle the specific sidenav to close
       */
      close: function(handle) {
        if(!instance) { return; }
        instance.close();
      }
    }
  }
}

/**
 * @ngdoc directive
 * @name materialSidenav
 * @restrict E
 *
 * @description
 *
 * A Sidenav component that can be opened and closed programatically.
 *
 * @example
 * <material-sidenav>
 * </material-sidenav>
 */
function materialSidenavDirective() {
  return {
    restrict: 'E',
    transclude: true,
    scope: {},
    template: '<div class="material-sidenav-inner" ng-transclude></div>',
    controller: '$materialSidenavController',
    link: function($scope, $element, $attr) {
      $scope.$watch('isOpen', function(v) {
        if(v) {
          $element.addClass('open');
        } else {
          $element.removeClass('open');
        }
      });
    }
  };
}

/**
 * @ngdoc module
 * @name material.components.slider
 * @description Slider module!
 */
angular.module('material.components.slider', [])
  .directive('materialSlider', [ '$window', materialSliderDirective ]);

/**
 * @ngdoc directive
 * @name materialSlider
 * @module material.components.slider
 * @restrict E
 *
 * @description
 * Slider directive!
 *
 */
function materialSliderDirective($window) {

  var MIN_VALUE_CSS = 'material-slider-min';
  var ACTIVE_CSS = 'material-active';

  function rangeSettings(rangeEle) {
    return {
      min: parseInt( rangeEle.min !== "" ? rangeEle.min : 0, 10 ),
      max: parseInt( rangeEle.max !== "" ? rangeEle.max : 100, 10 ),
      step: parseInt( rangeEle.step !== "" ? rangeEle.step : 1, 10 )
    }
  }

  return {
    restrict: 'E',
    scope: true,
    transclude: true,
    template: '<div class="material-track" ng-transclude></div>',
    link: link
  };

  // **********************************************************
  // Private Methods
  // **********************************************************

  function link(scope, element, attr) {
    var input = element.find('input');
    var ngModelCtrl = angular.element(input).controller('ngModel');

    if(!ngModelCtrl || input[0].type !== 'range') return;

    var rangeEle = input[0];
    var trackEle = angular.element( element[0].querySelector('.material-track') );

    trackEle.append('<div class="material-fill"><div class="material-thumb"></div></div>');
    var fillEle = trackEle[0].querySelector('.material-fill');

    if(input.attr('step')) {
      var settings = rangeSettings(rangeEle);
      var tickCount = (settings.max - settings.min) / settings.step;
      var tickMarkersEle = angular.element('<div class="material-tick-markers material-display-flex"></div>');
      for(var i=0; i<tickCount; i++) {
        tickMarkersEle.append('<div class="material-tick material-flex"></div>');
      }
      trackEle.append(tickMarkersEle);
    }

    input.on('mousedown touchstart', function(e){
      trackEle.addClass(ACTIVE_CSS);
    });

    input.on('mouseup touchend', function(e){
      trackEle.removeClass(ACTIVE_CSS);
    });


    function render() {
      var settings = rangeSettings(rangeEle);
      var adjustedValue = parseInt(ngModelCtrl.$viewValue, 10) - settings.min;
      var fillRatio = (adjustedValue / (settings.max - settings.min));

      fillEle.style.width = (fillRatio * 100) + '%';

      if(fillRatio <= 0) {
        element.addClass(MIN_VALUE_CSS);
      } else {
        element.removeClass(MIN_VALUE_CSS);
      }

    }

    scope.$watch( function () { return ngModelCtrl.$viewValue; }, render );

  }

}


angular.module('material.components.tabs', ['material.utils', 'material.animations', 'material.services'])
  .controller('materialTabsController', [ '$iterator', '$scope', TabsController])
  .directive('materialTabs', [ '$compile', 'materialEffects', TabsDirective ])
  .directive('materialTab', [ '$attrBind', '$compile', '$timeout', TabDirective  ]);

/**
 * @ngdoc directive
 * @name materialTabs
 * @module material.components.tabs
 *
 * @restrict E
 *
 * @description
 * materialTabs is the outer directive and container for the tabs functionality
 *
 * @param {integer=} selected Index of the active/selected tab
 * @param {boolean}  noink Flag indicates use of RippleInk effects
 * @param {boolean}  nobar Flag indicates use of InkBar effects
 * @param {boolean}  nostretch Flag indicates use of elastic animation for inkBar width and position changes
 * @param {string}   align-tabs Attribute to indicate position of tab buttons: bottom or top; default is `top`
 *
 * @example
 <example module="material.components.tabs">
 <file name="index.html">
 <h3>Static Tabs: </h3>
 <p>No ink effect and no sliding bar. Tab #1 is active and #2 is disabled.</p>
 <material-tabs selected="0" noink nobar nostretch>
 <material-tab>ITEM ONE</material-tab>
 <material-tab disabled="true" title="ITEM TWO"></material-tab>
 <material-tab>ITEM THREE</material-tab>
 </material-tabs>
 </file>
 </example>
 *
 */

function TabsDirective($compile, materialEffects) {

  return {
    restrict: 'E',
    replace: false,
    transclude: 'true',

    scope: {
      $selIndex: '=?selected'
    },

    compile: compileTabsFn,
    controller: [ '$scope', '$iterator', '$attrs', '$materialComponentRegistry', '$timeout', TabsController ],

    template:
      '<div class="tabs-header">' +
      '  <div class="tabs-header-items"></div>' +
      '  <shadow></shadow>' +
      '  <material-ink-bar></material-ink-bar>'  +
      '</div>'+
      '<div class="tabs-content ng-hide"></div>'

  };

  /**
   * Use prelink to configure inherited scope attributes: noink, nobar, and nostretch;
   * do this before the child elements are linked.
   *
   * @param element
   * @param attr
   * @returns {{pre: materialTabsLink}}
   */
  function compileTabsFn() {

    return {
      pre: function tabsPreLink(scope, element, attrs, tabsController) {

        // These attributes do not have values; but their presence defaults to value == true.
        scope.noink = angular.isDefined(attrs.noink);
        scope.nobar = angular.isDefined(attrs.nobar);
        scope.nostretch = angular.isDefined(attrs.nostretch);

        // Publish for access by nested `<material-tab>` elements
        tabsController.noink = scope.noink;

        // Watch for external changes `selected` & auto-select the specified tab
        // Stop watching when the <material-tabs> directive is released
        scope.$on("$destroy", scope.$watch('$selIndex', function (index) {
          tabsController.selectAt(index);
        }));

        // Remove the `inkBar` element if `nobar` is defined
        var elBar = findNode("material-ink-bar",element);
        if ( elBar && scope.nobar ) {
          elBar.remove();
        }

      },
      post: function tabsPostLink(scope, element, attrs, tabsController, $transclude) {

        alignTabButtons();
        transcludeHeaderItems();
        transcludeContentItems();

        configureInk();

        selectDefaultTab();

        // **********************************************************
        // Private Methods
        // **********************************************************

        /**
         * Conditionally configure ink bar animations when the
         * tab selection changes. If `nobar` then do not show the
         * bar nor animate.
         */
        function configureInk() {
          if ( scope.nobar ) return;

          // Single inkBar is used for all tabs
          var inkBar = findNode("material-ink-bar", element);

          // On resize or tabChange
          tabsController.onTabChange = updateInkBar;
          angular.element(window).on('resize', function() {
            updateInkBar(tabsController.selectedElement(), true);
          });

          // Immediately place the ink bar
          updateInkBar(tabsController.selectedElement(), true );

          /**
           * Update the position and size of the ink bar based on the
           * specified tab DOM element
           * @param tab
           * @param skipAnimation
           */
          function updateInkBar(tab, skipAnimation) {
            if ( angular.isDefined(tab) && angular.isDefined(inkBar) ) {

              var tabNode = tab[0];
              var width = ( tabsController.$$tabs().length > 1 ) ? tabNode.offsetWidth : 0;
              var styles = {
                left : tabNode.offsetLeft +'px',
                width : width +'px' ,
                display : width > 0 ? 'block' : 'none'
              };

              if( !!skipAnimation ) {
                inkBar.css(styles);
              } else {
                materialEffects.inkBar(inkBar, styles);
              }
            }

          }
        }

        /**
         * Change the positioning of the tab header and buttons.
         * If the tabs-align attribute is 'bottom', then the tabs-content
         * container is transposed with the tabs-header
         */
        function alignTabButtons() {
          var align  = attrs['tabsAlign'] || "top";
          var content = findNode('.tabs-content', element);

          if ( align == "bottom") {
            element.prepend(content);
          }
        }

        /**
         * Transclude the materialTab items into the tabsHeaderItems container
         *
         */
        function transcludeHeaderItems() {
          $transclude(function (content) {
            var header = findNode('.tabs-header-items', element);
            var parent = angular.element(element[0]);

            angular.forEach(content, function (node) {
              var intoHeader = isNodeType(node, 'material-tab') || isNgRepeat(node);

              if (intoHeader) {
                header.append(node);
              }
              else {
                parent.prepend(node);
              }
            });
          });
        }

        /**
         * If an initial tab selection has not been specified, then
         * select the first tab by default
         */
        function selectDefaultTab() {
          var tabs = tabsController.$$tabs();

          if ( tabs.length && angular.isUndefined(scope.$selIndex)) {
            tabsController.select(tabs[0]);
          }
        }

        /**
         * Transclude the materialTab view/body contents into materialView containers; which
         * are stored in the tabsContent area...
         */
        function transcludeContentItems() {
          var cache = {
              length: 0,
              contains: function (tab) {
                return !angular.isUndefined(cache[tab.$id]);
              }
            },
            cntr = findNode('.tabs-content', element),
            materialViewTmpl = '<div class="material-view" ng-show="active"></div>';

          scope.$watch(getTabsHash, function buildContentItems() {
            var tabs = tabsController.$$tabs(notInCache),
              views = tabs.map(extractViews);

            // At least 1 tab must have valid content to build; otherwise
            // we hide/remove the tabs-content container...

            if (views.some(notEmpty)) {
              angular.forEach(views, function (elements, j) {

                var tab = tabs[j++],
                  materialView = $compile(materialViewTmpl)(tab);

                if (elements) {
                  // If transcluded content is not undefined then add all nodes to the materialView
                  angular.forEach(elements, function (node) {
                    materialView.append(node);
                  });
                }

                cntr.append(materialView);
                addToCache(cache, { scope: tab, element: materialView });

              });
            }

            // Hide or Show the container for the materialView(s)
            angular.bind(cntr, cache.length ? cntr.removeClass : cntr.addClass)('ng-hide');

          });

          /**
           * Add tab scope/DOM node to the cache and configure
           * to auto-remove when the scope is destroyed.
           * @param cache
           * @param item
           */
          function addToCache(cache, item) {

            cache[ item.scope.$id ] = item;
            cache.length = cache.length + 1;

            // When the tab is removed, remove its associated material-view Node...
            item.scope.$on("$destroy", function () {
              angular.element(item.element).remove();

              delete cache[ item.scope.$id];
              cache.length = cache.length - 1;
            });
          }

          function getTabsHash() {
            return tabsController.$$hash;
          }

          function extractViews(tab) {
            return hasContent(tab) ? tab.content : undefined;
          }

          function hasContent(tab) {
            return tab.content && tab.content.length;
          }

          function notEmpty(view) {
            return angular.isDefined(view);
          }

          function notInCache(tab) {
            return !cache.contains(tab);
          }
        }

      }
    };

    function findNode(selector, element) {
      var container = element[0];
      return angular.element(container.querySelector(selector));
    }

  }

}

/**
 /**
 * @ngdoc directive
 * @name materialTab
 * @module material.components.tabs
 *
 * @restrict E
 *
 * @param {string=} onSelect A function expression to call when the tab is selected.
 * @param {string=} onDeselect A function expression to call when the tab is deselected.
 * @param {boolean=} active A binding, telling whether or not this tab is selected.
 * @param {boolean=} disabled A binding, telling whether or not this tab is disabled.
 * @param {string=} title The visible heading, or title, of the tab. Set HTML headings with {@link ui.bootstrap.tabs.directive:tabHeading tabHeading}.
 *
 * @description
 * Creates a tab with a heading and (optional) content. Must be placed within a {@link material.components.tabs.directive:materialTabs materialTabs}.
 *
 * @example
 *
 */
function TabDirective($attrBind, $compile, $timeout) {
  var noop = angular.noop;

  return {
    restrict: 'E',
    replace: false,
    require: "^materialTabs",
    transclude: 'true',
    scope: true,
    link: linkTab,
    template:
      '<material-ripple initial-opacity="0.9" opacity-decay-velocity="0.89"> </material-ripple> ' +
      '<material-tab-label ' +
        'ng-class="{ disabled : disabled, active : active }"  >' +
      '</material-tab-label>'

  };

  function linkTab(scope, element, attrs, tabsController, $transclude) {
    var defaults = { active: false, disabled: false, deselected: noop, selected: noop };

    // Since using scope=true for inherited new scope,
    // then manually scan element attributes for forced local mappings...

    $attrBind(scope, attrs, {
      label: '@?',
      active: '=?',
      disabled: '=?',
      deselected: '&onDeselect',
      selected: '&onSelect'
    }, defaults);

    configureEffects();
    configureWatchers();
    updateTabContent(scope);

    // Click support for entire <material-tab /> element
    element.on('click', function onRequestSelect() {
      if (!scope.disabled) {
        scope.$apply(function () {
          tabsController.select(scope);
        })
      }
    });

    tabsController.add(scope, element);

    // **********************************************************
    // Private Methods
    // **********************************************************

    /**
     * If materialTabs `noInk` is true, then remove the materialInkBar feature
     * By default, the materialInkBar tag is auto injected; @see line 255
     */
    function configureEffects() {
      if ( tabsController.noink ) {

        // Since <material-ripple/> directive replaces itself with `<div.material-ink-ripple />` element
        var elRipple = angular.element(element[0].querySelector('.material-ink-ripple'));
        if (elRipple) {
          elRipple.remove();
        }
      }
    }

    /**
     * Auto select the next tab if the current tab is active and
     * has been disabled.
     */
    function configureWatchers() {
      var unwatch = scope.$watch('disabled', function (isDisabled) {
        if (scope.active && isDisabled) {
          tabsController.next(scope);
        }
      });

      scope.$on("$destroy", function () {
        unwatch();
        tabsController.remove(scope);
      });
    }

    /**
     * Transpose the optional `label` attribute value or materialTabHeader or `content` body
     * into the body of the materialTabButton... all other content is saved in scope.content
     * and used by TabsController to inject into the `tabs-content` container.
     */
    function updateTabContent(scope) {
      var cntr = angular.element(element[0].querySelector('material-tab-label'));

      // Check to override label attribute with the content of the <material-tab-header> node,
      // If a materialTabHeader is not specified, then the node will be considered
      // a <material-view> content element...

      $transclude(function (contents) {
        scope.content = [ ];

        angular.forEach(contents, function (node) {
          if (!isNodeEmpty(node)) {
            if (isNodeType(node, 'material-tab-label')) {
              // Simulate use of `label` attribute
              scope.label = node.childNodes;

            } else {

              // Attach to scope for future transclusion into materialView(s)
              scope.content.push(node);
            }
          }
        });

      });

      // Prepare to assign the materialTabButton content
      // Use the label attribute or fallback to TabHeader content

      if (angular.isDefined(scope.label)) {
        // The `label` attribute is the default source

        cntr.append(scope.label);

      } else {

        // NOTE: If not specified, all markup and content is assumed
        // to be used for the tab label.

        angular.forEach(scope.content, function (node) {
          cntr.append(node);
        });

        delete scope.content;
      }
    }

  }
}

/**
 * @ngdoc controller
 * @name materialTabsController
 * @module material.components.tabs
 *
 * @private
 *
 */
function TabsController($scope, $iterator, $attrs, $materialComponentRegistry, $timeout) {
  var list = $iterator([], true),
    elements = { },
    selected = null,
    self = this;

  $materialComponentRegistry.register(self, $attrs.componentId || "tabs");

  // Methods used by <material-tab> and children

  this.add = addTab;
  this.remove = removeTab;
  this.select = selectTab;
  this.selectAt = selectTabAt;
  this.next = selectNext;
  this.previous = selectPrevious;

  // Property for child access
  this.noink = !!$scope.noink;
  this.nobar = !!$scope.nobar;
  this.scope = $scope;

  // Special internal accessor to access scopes and tab `content`
  // Used by TabsDirective::buildContentItems()

  this.$$tabs = findTabs;
  this.$$hash = "";

  // used within the link-Phase of materialTabs
  this.onTabChange = angular.noop;
  this.selectedElement = function() {
    return findElementFor( selected );
  }

  /**
   * Find the DOM element associated with the tab/scope
   * @param tab
   * @returns {*}
   */
  function findElementFor(tab) {
    if ( angular.isUndefined(tab) ) {
      tab = selected;
    }
    return tab ? elements[ tab.$id ] : undefined;
  }

  /**
   * Publish array of tab scope items
   * NOTE: Tabs are not required to have `contents` and the
   *       node may be undefined.
   * @returns {*} Array
   */
  function findTabs(filterBy) {
    return list.items().filter(filterBy || angular.identity);
  }

  /**
   * Create unique hashKey representing all available
   * tabs.
   */
  function updateHash() {
    self.$$hash = list.items()
      .map(function (it) {
        return it.$id;
      })
      .join(',');
  }

  /**
   * Select specified tab; deselect all others (if any selected)
   * @param tab
   */
  function selectTab(tab) {
    var activate = makeActivator(true),
      deactivate = makeActivator(false);

    // Turn off all tabs (if current active)
    angular.forEach(list.items(), deactivate);

    // Activate the specified tab (or next available)
    selected = activate(tab.disabled ? list.next(tab) : tab);

    // update external models and trigger databinding watchers
    $scope.$selIndex = String(selected.$index || list.indexOf(selected));

    // update the tabs ink to indicate the selected tab
    self.onTabChange( findElementFor(selected) );

    return selected;
  }

  /**
   * Select tab based on its index position
   * @param index
   */
  function selectTabAt(index) {
    if (list.inRange(index)) {
      var matches = list.findBy("$index", index),
        it = matches ? matches[0] : null;

      if (it != selected) {
        selectTab(it);
      }
    }
  }

  /**
   * If not specified (in parent scope; as part of ng-repeat), create
   * `$index` property as part of current scope.
   * NOTE: This prevents scope variable shadowing...
   * @param tab
   * @param index
   */
  function updateIndex(tab, index) {
    if (angular.isUndefined(tab.$index)) {
      tab.$index = index;
    }
  }

  /**
   * Add tab to list and auto-select; default adds item to end of list
   * @param tab
   */
  function addTab(tab, element) {

    updateIndex(tab, list.count());

    // cache materialTab DOM element; these are not materialView elements
    elements[ tab.$id ] = element;

    if (!list.contains(tab)) {
      var pos = list.add(tab, tab.$index);

      // Should we auto-select it?
      if ($scope.$selIndex == pos) {
        selectTab(tab);
      }
    }


    updateHash();

    return tab.$index;
  }

  /**
   * Remove the specified tab from the list
   * Auto select the next tab or the previous tab (if last)
   * @param tab
   */
  function removeTab(tab) {
    if (list.contains(tab)) {

      selectTab(selected = list.next(tab, isEnabled));
      list.remove(tab);

      // another tab was removed, make sure to update ink bar
      $timeout(function(){
        self.onTabChange( findElementFor(selected), true );
        delete elements[tab.$id];
      },300);

    }

    updateHash();
  }

  /**
   * Select the next tab in the list
   * @returns {*} Tab
   */
  function selectNext() {
    return selectTab(selected = list.next(selected, isEnabled));
  }

  /**
   * Select the previous tab
   * @returns {*} Tab
   */
  function selectPrevious() {
    return selectTab(selected = list.previous(selected, isEnabled));
  }

  /**
   * Validation criteria for list iterator when List::next() or List::previous() is used..:
   * In this case, the list iterator should skip items that are disabled.
   * @param tab
   * @returns {boolean}
   */
  function isEnabled(tab) {
    return tab && !tab.disabled;
  }

  /**
   * Partial application to build function that will
   * mark the specified tab as active or not. This also
   * allows the `updateStatus` function to be used as an iterator.
   *
   * @param active
   */
  function makeActivator(active) {
    return function updateState(tab) {
      if (tab && (active != tab.active)) {
        tab.active = active;

//        Disable ripples when tab is active/selected
//        tab.inkEnabled = !active;

        tab.inkEnabled = true;

        if (active) {
          selected = tab;
          tab.selected();
        } else {
          if (selected == tab) {
            selected = null;
          }
          tab.deselected();
        }
        return tab;
      }
      return null;

    }
  }

}

var trim = (function () {
  function isString(value) {
    return typeof value === 'string';
  }

  // native trim is way faster: http://jsperf.com/angular-trim-test
  // but IE doesn't have it... :-(
  // TODO: we should move this into IE/ES5 polyfill
  if (!String.prototype.trim) {
    return function (value) {
      return isString(value) ? value.replace(/^\s\s*/, '').replace(/\s\s*$/, '') : value;
    };
  }
  return function (value) {
    return isString(value) ? value.trim() : value;
  };
})();

/**
 * Determine if the DOM element is of a certain tag type
 * or has the specified attribute type
 *
 * @param node
 * @returns {*|boolean}
 */
var isNodeType = function (node, type) {
  return node.tagName && (
    node.hasAttribute(type) ||
    node.hasAttribute('data-' + type) ||
    node.tagName.toLowerCase() === type ||
    node.tagName.toLowerCase() === 'data-' + type
    );
};

var isNgRepeat = function (node) {
  var COMMENT_NODE = 8;
  return (node.nodeType == COMMENT_NODE) && (node.nodeValue.indexOf('ngRepeat') > -1);
};

/**
 * Is the an empty text string
 * @param node
 * @returns {boolean}
 */
var isNodeEmpty = function (node) {
  var TEXT_NODE = 3;
  return (node.nodeType == TEXT_NODE) && (trim(node.nodeValue) == "");
};


angular.module('material.components.toast', ['material.services.popup'])
  .directive('materialToast', [
    QpToastDirective
  ])
  /**
   * @ngdoc service
   * @name $materialToast
   * @module material.components.toast
   */
  .factory('$materialToast', [
    '$timeout',
    '$materialPopup',
    QpToastService
  ]);

function QpToastDirective() {
  return {
    restrict: 'E',
    transclude: true,
    template: 
      '<div class="toast-container" ng-transclude>' +
      '</div>'
  };
}

function QpToastService($timeout, $materialPopup) {
  var recentToast;

  return showToast;

  /**
   * TODO fully document this
   * Supports all options from $materialPopup, in addition to `duration` and `position`
   */
  function showToast(options) {
    options = angular.extend({
      // How long to keep the toast up, milliseconds
      duration: 3000,
      // [unimplemented] Whether to disable swiping
      swipeDisabled: false,
      // Supports any combination of these class names: 'bottom top left right fit'. 
      // Default: 'bottom left'
      position: 'bottom left',

      // Also supports all options from $materialPopup
      transformTemplate: function(template) {
        return '<material-toast>' + template + '</material-toast>';
      }
    }, options || {});

    recentToast && recentToast.then(function(destroyToast) {
      destroyToast();
    });

    recentToast = $materialPopup(options).then(function(toast) {
      function destroy() {
        $timeout.cancel(toast.delay);
        toast.destroy();
      }

      // Controller will be passed a `$hideToast` function
      toast.locals.$hideToast = destroy;

      toast.element.addClass(options.position);
      toast.enter(function() {
        if (options.duration) {
          toast.delay = $timeout(destroy, options.duration);
        }
      });

      return destroy;
    });

    return recentToast;
  }
}

angular.module('material.components.toolbar', [
  'material.components.content'
])
  .directive('materialToolbar', [materialToolbarDirective]);

function materialToolbarDirective() {

  return {
    restrict: 'E',
    transclude: true,
    template: '<div class="material-toolbar-inner" ng-transclude></div>'
  }

}

angular.module('material.components.whiteframe', []);

angular.module( 'material.services', [
  'material.services.throttle',
  'material.services.registry',
  'material.services.position',
  'material.services.popup',
  'material.services.compiler'
]);

/**
 * @ngdoc overview
 * @name material.services.registry
 *
 * @description
 * A component registry system for accessing various component instances in an app.
 */
angular.module('material.services.registry', [])
  .factory('$materialComponentRegistry', [ '$log', materialComponentRegistry ]);

/**
 * @ngdoc service
 * @name material.services.registry.service:$materialComponentRegistry
 *
 * @description
 * $materialComponentRegistry enables the user to interact with multiple instances of
 * certain complex components in a running app.
 */
function materialComponentRegistry($log) {
  var instances = [];

  return {
    /**
     * Used to print an error when an instance for a handle isn't found.
     */
    notFoundError: function(handle) {
      $log.error('No instance found for handle', handle);
    },
    /**
     * Return all registered instances as an array.
     */
    getInstances: function() {
      return instances;
    },

    /**
     * Get a registered instance.
     * @param handle the String handle to look up for a registered instance.
     */
    get: function(handle) {
      var i, j, instance;
      for(i = 0, j = instances.length; i < j; i++) {
        instance = instances[i];
        if(instance.$$materialHandle === handle) {
          return instance;
        }
      }
      return null;
    },

    /**
     * Register an instance.
     * @param instance the instance to register
     * @param handle the handle to identify the instance under.
     */
    register: function(instance, handle) {
      instance.$$materialHandle = handle;
      instances.push(instance);

      return function deregister() {
        var index = instances.indexOf(instance);
        if (index !== -1) {
          instances.splice(index, 1);
        }
      };
    },
  }
}


angular.module('material.services.compiler', [])
  .service('$materialCompiler', [
    '$q',
    '$http',
    '$injector',
    '$compile',
    '$controller',
    '$templateCache',
    materialCompilerService
  ]);

function materialCompilerService($q, $http, $injector, $compile, $controller, $templateCache) {

  /**
   * @ngdoc service
   * @name $materialCompiler
   * @module material.services.compiler
   *
   * @description
   * The $materialCompiler service is an abstraction of angular's compiler, that allows the developer
   * to easily compile an element with a templateUrl, controller, and locals.
   */

   /**
    * @ngdoc method
    * @name $materialCompiler#compile
    * @param {object} options An options object, with the following properties:
    *
    *    - `controller` â€“ `{(string=|function()=}` â€“ Controller fn that should be associated with
    *      newly created scope or the name of a {@link angular.Module#controller registered
    *      controller} if passed as a string.
    *    - `controllerAs` â€“ `{string=}` â€“ A controller alias name. If present the controller will be
    *      published to scope under the `controllerAs` name.
    *    - `template` â€“ `{string=}` â€“ html template as a string or a function that
    *      returns an html template as a string which should be used by {@link
    *      ngRoute.directive:ngView ngView} or {@link ng.directive:ngInclude ngInclude} directives.
    *      This property takes precedence over `templateUrl`.
    *
    *    - `templateUrl` â€“ `{string=}` â€“ path or function that returns a path to an html
    *      template that should be used by {@link ngRoute.directive:ngView ngView}.
    *
    *    - `transformTemplate` â€“ `{function=} â€“ a function which can be used to transform
    *      the templateUrl or template provided after it is fetched.  It will be given one
    *      parameter, the template, and should return a transformed template.
    *
    *    - `resolve` - `{Object.<string, function>=}` - An optional map of dependencies which should
    *      be injected into the controller. If any of these dependencies are promises, the compiler
    *      will wait for them all to be resolved or one to be rejected before the controller is
    *      instantiated.
    *
    *      - `key` â€“ `{string}`: a name of a dependency to be injected into the controller.
    *      - `factory` - `{string|function}`: If `string` then it is an alias for a service.
    *        Otherwise if function, then it is {@link api/AUTO.$injector#invoke injected}
    *        and the return value is treated as the dependency. If the result is a promise, it is
    *        resolved before its value is injected into the controller.
    *
    * @returns {object=} promise A promsie which will be resolved with a `compileData` object,
    * with the following properties:
    *
    *   - `{element}` â€“ `element` â€“ an uncompiled angular element compiled using the provided template.
    *   
    *   - `{function(scope)}`  â€“ `link` â€“ A link function, which, when called, will compile
    *     the elmeent and instantiate options.controller.
    *
    *   - `{object}` â€“ `locals` â€“ The locals which will be passed into the controller once `link` is
    *     called.
    *
    * @usage
    * $materialCompiler.compile({
    *   templateUrl: 'modal.html',
    *   controller: 'ModalCtrl',
    *   locals: {
    *     modal: myModalInstance;
    *   }
    * }).then(function(compileData) {
    *   compileData.element; // modal.html's template in an element
    *   compileData.link(myScope); //attach controller & scope to element
    * });
    */
  this.compile = function(options) {
    var templateUrl = options.templateUrl;
    var template = options.template || '';
    var controller = options.controller;
    var controllerAs = options.controllerAs;
    var resolve = options.resolve || {};
    var locals = options.locals || {};
    var transformTemplate = options.transformTemplate || angular.identity;

    // Take resolve values and invoke them.  
    // Resolves can either be a string (value: 'MyRegisteredAngularConst'),
    // or an invokable 'factory' of sorts: (value: function ValueGetter($dependency) {})
    angular.forEach(resolve, function(value, key) {
      if (angular.isString(value)) {
        resolve[key] = $injector.get(value);
      } else {
        resolve[key] = $injector.invoke(value);
      }
    });
    //Add the locals, which are just straight values to inject
    //eg locals: { three: 3 }, will inject three into the controller
    angular.extend(resolve, locals);

    if (templateUrl) {
      resolve.$template = $http.get(templateUrl, {cache: $templateCache})
        .then(function(response) {
          return response.data;
        });
    } else {
      resolve.$template = $q.when(template);
    }

    // Wait for all the resolves to finish if they are promises
    return $q.all(resolve).then(function(locals) {

      var template = transformTemplate(locals.$template);
      var element = angular.element('<div>').html(template).contents();
      var linkFn = $compile(element);

      //Return a linking function that can be used later whne the element is ready
      return {
        locals: locals,
        element: element,
        link: function link(scope) {
          locals.$scope = scope;

          //Instantiate controller if it exists, because we have scope
          if (controller) {
            var ctrl = $controller(controller, locals);
            //See angular-route source for this logic
            element.data('$ngControllerController', ctrl);
            element.children().data('$ngControllerController', ctrl);

            if (controllerAs) {
              scope[controllerAs] = ctrl;
            }
          }

          return linkFn(scope);
        }
      };
    });
  };
}

angular.module('material.services.popup', ['material.services.compiler'])

  .factory('$materialPopup', [
    '$materialCompiler',
    '$animate',
    '$rootScope',
    '$rootElement',
    PopupFactory
  ]);

function PopupFactory($materialCompiler, $animate, $rootScope, $rootElement) {

  return createPopup;

  function createPopup(options) {
    var appendTo = options.appendTo || $rootElement;
    var scope = (options.scope || $rootScope).$new();

    return $materialCompiler.compile(options).then(function(compileData) {
      var self;

      return self = angular.extend({
        enter: enter,
        leave: leave,
        destroy: destroy,
        scope: scope
      }, compileData);

      function enter(done) {
        if (scope.$$destroyed || self.entered) return (done || angular.noop)();

        self.entered = true;
        var after = appendTo[0].lastElementChild;
        $animate.enter(self.element, appendTo, after && angular.element(after), done);

        //On the first enter, compile the element
        if (!self.compiled) {
          compileData.link(scope);
          self.compiled = true;
        }
      }
      function leave(done) {
        self.entered = false;
        $animate.leave(self.element, done);
      }
      function destroy(done) {
        if (scope.$$destroyed) return (done || angular.noop)();
        self.leave(function() {
          scope.$destroy();
          (done || angular.noop)();
        });
      }
    });
  }
}

/**
 * Adapted from ui.bootstrap.position
 * https://github.com/angular-ui/bootstrap/blob/master/src/position/position.js
 * https://github.com/angular-ui/bootstrap/blob/master/LICENSE
 */

angular.module('material.services.position', ['ui.bootstrap.position']);

angular.module('ui.bootstrap.position', [])

/**
 * A set of utility methods that can be use to retrieve position of DOM elements.
 * It is meant to be used where we need to absolute-position DOM elements in
 * relation to other, existing elements (this is the case for tooltips, popovers,
 * typeahead suggestions etc.).
 */
  .factory('$position', ['$document', '$window', function ($document, $window) {

    function getStyle(el, cssprop) {
      if (el.currentStyle) { //IE
        return el.currentStyle[cssprop];
      } else if ($window.getComputedStyle) {
        return $window.getComputedStyle(el)[cssprop];
      }
      // finally try and get inline style
      return el.style[cssprop];
    }

    /**
     * Checks if a given element is statically positioned
     * @param element - raw DOM element
     */
    function isStaticPositioned(element) {
      return (getStyle(element, 'position') || 'static' ) === 'static';
    }

    /**
     * returns the closest, non-statically positioned parentOffset of a given element
     * @param element
     */
    var parentOffsetEl = function (element) {
      var docDomEl = $document[0];
      var offsetParent = element.offsetParent || docDomEl;
      while (offsetParent && offsetParent !== docDomEl && isStaticPositioned(offsetParent) ) {
        offsetParent = offsetParent.offsetParent;
      }
      return offsetParent || docDomEl;
    };

    return {
      /**
       * Provides read-only equivalent of jQuery's position function:
       * http://api.jquery.com/position/
       */
      position: function (element) {
        var elBCR = this.offset(element);
        var offsetParentBCR = { top: 0, left: 0 };
        var offsetParentEl = parentOffsetEl(element[0]);
        if (offsetParentEl != $document[0]) {
          offsetParentBCR = this.offset(angular.element(offsetParentEl));
          offsetParentBCR.top += offsetParentEl.clientTop - offsetParentEl.scrollTop;
          offsetParentBCR.left += offsetParentEl.clientLeft - offsetParentEl.scrollLeft;
        }

        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: boundingClientRect.width || element.prop('offsetWidth'),
          height: boundingClientRect.height || element.prop('offsetHeight'),
          top: elBCR.top - offsetParentBCR.top,
          left: elBCR.left - offsetParentBCR.left
        };
      },

      /**
       * Provides read-only equivalent of jQuery's offset function:
       * http://api.jquery.com/offset/
       */
      offset: function (element) {
        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: boundingClientRect.width || element.prop('offsetWidth'),
          height: boundingClientRect.height || element.prop('offsetHeight'),
          top: boundingClientRect.top + ($window.pageYOffset || $document[0].documentElement.scrollTop),
          left: boundingClientRect.left + ($window.pageXOffset || $document[0].documentElement.scrollLeft)
        };
      },

      /**
       * Provides coordinates for the targetEl in relation to hostEl
       */
      positionElements: function (hostEl, targetEl, positionStr, appendToBody) {

        var positionStrParts = positionStr.split('-');
        var pos0 = positionStrParts[0], pos1 = positionStrParts[1] || 'center';

        var hostElPos,
          targetElWidth,
          targetElHeight,
          targetElPos;

        hostElPos = appendToBody ? this.offset(hostEl) : this.position(hostEl);

        targetElWidth = targetEl.prop('offsetWidth');
        targetElHeight = targetEl.prop('offsetHeight');

        var shiftWidth = {
          center: function () {
            return hostElPos.left + hostElPos.width / 2 - targetElWidth / 2;
          },
          left: function () {
            return hostElPos.left;
          },
          right: function () {
            return hostElPos.left + hostElPos.width;
          }
        };

        var shiftHeight = {
          center: function () {
            return hostElPos.top + hostElPos.height / 2 - targetElHeight / 2;
          },
          top: function () {
            return hostElPos.top;
          },
          bottom: function () {
            return hostElPos.top + hostElPos.height;
          }
        };

        switch (pos0) {
          case 'right':
            targetElPos = {
              top: shiftHeight[pos1](),
              left: shiftWidth[pos0]()
            };
            break;
          case 'left':
            targetElPos = {
              top: shiftHeight[pos1](),
              left: hostElPos.left - targetElWidth
            };
            break;
          case 'bottom':
            targetElPos = {
              top: shiftHeight[pos0](),
              left: shiftWidth[pos1]()
            };
            break;
          default:
            targetElPos = {
              top: shiftHeight[pos0](),
              left: shiftWidth[pos1]()
            };
            break;
        }

        return targetElPos;
      }
    };
  }]);

angular.module('material.services.throttle', [ 'ng' ])
  /**
   *
   *   var ripple, watchMouse,
   *       parent = element.parent(),
   *       makeRipple = $throttle({
   *         start : function() {
   *           ripple = ripple || materialEffects.inkRipple( element[0], options );
   *           watchMouse = watchMouse || buildMouseWatcher(parent, makeRipple);
   *           // Ripples start with mouseDow (or taps)
   *           parent.on('mousedown', makeRipple);
   *         },
   *         throttle : function(e, done) {
   *           if ( effectAllowed() )
   *           {
   *             switch(e.type)
   *             {
   *               case 'mousedown' :
   *                 watchMouse(true);
   *                 ripple.createAt( options.forceToCenter ? null : localToCanvas(e) );
   *                 break;
   *               default:
   *                 watchMouse(false);
   *                 ripple.draw( localToCanvas(e) );
   *                 break;
   *             }
   *           } else {
   *             done();
   *           }
   *         },
   *         end : function() {
   *           watchMouse(false);
   *         }
   *       });
   *
   *   makeRipple();
   *
   */
  .factory( "$throttle", ['$timeout', '$$q', '$log', function ($timeout, $$q, $log) {

      var STATE_READY= 0, STATE_START=1, STATE_THROTTLE=2, STATE_END=3;

      return function( config ){
        return function( done, otherwise ){
          return buildInstance( angular.extend({}, config), done || angular.noop, otherwise || angular.noop )
        };
      };

      function buildInstance( phases, done, otherwise ) {
        var pendingActions = [ ],
            cancel = angular.noop,
            state = STATE_READY;

        // Defer the call to the start function ... so `throttle` reference can be returned...
        $timeout(function(){
          start().then(function(){
             if ( phases.throttle == null ) {
               end();
             }
          });
        },0,false);

        return throttle;

        /**
         * Facade function that validates throttler
         * state BEFORE processing the `throttle` request.
         */
        function throttle( data, done ) {

          if ( state != STATE_THROTTLE ) {
              cacheRquest();
          }

          switch( state )
          {
            case STATE_READY :
              start();
              break;

            case STATE_START:
              break;

            // Proxy throttle call to custom, user-defined throttle handler
            case STATE_THROTTLE:
              invokeThrottleHandler(data, done);
              break;

            case STATE_END :
              restart();
              break;
          }

          // **********************************************************
          // Internal Methods
          // **********************************************************

          /**
           *  Cache for later submission to 'throttle()'
           */
          function cacheRquest() {
            pendingActions.push({ data:data, done:done });
          }

          /**
           * Delegate to the custom throttle function...
           * When CTF reports complete, then proceed to the `end` state
           *
           * @param data  Data to be delegated to the throttle function
           * @param done  Callback when all throttle actions have completed
           */
          function invokeThrottleHandler(data, done) {

            if ( angular.isFunction(phases.throttle) ) {
              done = done || angular.noop;

              try {

                phases.throttle.apply( null, [data, function(response) {
                  done.apply( null, [response] );
                  end();
                }]);

              } catch( error ) {
                // Report error... and end()

                otherwise(error);
                end();
              }

            } else {
              end();
            }
          }
        }


        /**
         * Initiate the async `start` phase of the Throttler
         * @returns {*} promise
         */
        function start() {
          return gotoState.apply(null, [ STATE_START, phases.start ] )
                          .then( feedPendingActions, otherwise );

          /**
           * Process all pending actions (if any)
           */
          function feedPendingActions( response ) {
            logResponse(response);

            state = STATE_THROTTLE;

            angular.forEach(pendingActions, function (it) {
              throttle( it.data, function(response) {
                logResponse(response);

                if ( angular.isFunction(it.done) ) {
                  it.done(response);
                }
              });
            });

            pendingActions = [ ];
          }
        }

        /**
         * Initiate the async `end` phase of the Throttler
         * @returns {*} promise
         */
        function end() {

          return gotoState.apply(null,[ STATE_END, phases.end ])
                          .then( finish, otherwise );

          /**
           * Mark throttle as ready to start... and announce completion
           * of the current activity cycle
           */
          function finish( response ) {
            logResponse(response);

            if ( state == STATE_END ){
              state = STATE_READY;
              done();
            }
          }

        }

        /**
         * Cancel any `end` process and restart state machine processes
         */
        function restart() {
          try {

            if ( !angular.isFunction(cancel) ) {
              cancel = angular.noop;
            }

            cancel();
            state = STATE_READY;

          } finally {

            start();
          }
        }

        /**
         * Change to next state and call the state function associated with that state...
         * @param nextState
         * @param targetFn
         * @returns {*}
         */
        function gotoState( nextState , targetFn  )
        {

          var dfd = $$q.defer(),
              hasFn = angular.isFunction(targetFn),
              goNext = hasFn && (targetFn.length < 1),
              fn = hasFn ? targetFn : resolved;

          try {

            state = nextState;

            cancel = fn.apply( null, [
              goNext ? resolved(dfd) :
              hasFn ? callbackToResolve(dfd) : dfd
            ]);

          } catch( error ) {
            dfd.reject( error );
          }

          return dfd.promise;
        }

      }

      // **********************************************************
      // Internal Methods
      // **********************************************************

      /**
       * Create callback function that will resolve the specified deferred.
       * @param dfd
       * @returns {Function}
       */
      function callbackToResolve( dfd )
      {
        return function(response){
          dfd.resolve.apply(null, [response ]);
        }
      }

      /**
       * Prepare fallback promise for start, end, throttle phases of the Throttler
       * @param dfd
       * @returns {*}
       */
      function resolved(dfd)
      {
        dfd = dfd || $$q.defer();
        dfd.resolve.apply(null, arguments.length > 1 ? [].slice.call(arguments,1) : [ ]);

        return dfd.promise;
      }

      function logResponse(response)
      {
        if ( angular.isDefined(response) ) {
          $log.debug(response);
        }
      }


  }]);





angular.module('material.utils', [ ])
  .factory('$attrBind', [ '$parse', '$interpolate', AttrsBinder ]);

/**
 *  This service allows directives to easily databind attributes to private scope properties.
 *
 * @private
 */
function AttrsBinder($parse, $interpolate) {
  var LOCAL_REGEXP = /^\s*([@=&])(\??)\s*(\w*)\s*$/;

  return function (scope, attrs, bindDefinition, bindDefaults) {
    angular.forEach(bindDefinition || {}, function (definition, scopeName) {
      //Adapted from angular.js $compile
      var match = definition.match(LOCAL_REGEXP) || [],
        attrName = match[3] || scopeName,
        mode = match[1], // @, =, or &
        parentGet,
        unWatchFn;

      switch (mode) {
        case '@':   // One-way binding from attribute into scope

          attrs.$observe(attrName, function (value) {
            scope[scopeName] = value;
          });
          attrs.$$observers[attrName].$$scope = scope;

          if (!bypassWithDefaults(attrName, scopeName)) {
            // we trigger an interpolation to ensure
            // the value is there for use immediately
            scope[scopeName] = $interpolate(attrs[attrName])(scope);
          }
          break;

        case '=':   // Two-way binding...

          if (!bypassWithDefaults(attrName, scopeName)) {
            // Immediate evaluation
            scope[scopeName] = scope.$eval(attrs[attrName]);

            // Data-bind attribute to scope (incoming) and
            // auto-release watcher when scope is destroyed

            unWatchFn = scope.$watch(attrs[attrName], function (value) {
              scope[scopeName] = value;
            });
            scope.$on('$destroy', unWatchFn);
          }

          break;

        case '&':   // execute an attribute-defined expression in the context of the parent scope

          if (!bypassWithDefaults(attrName, scopeName, angular.noop)) {
            /* jshint -W044 */
            if (attrs[attrName] && attrs[attrName].match(RegExp(scopeName + '\(.*?\)'))) {
              throw new Error('& expression binding "' + scopeName + '" looks like it will recursively call "' +
                attrs[attrName] + '" and cause a stack overflow! Please choose a different scopeName.');
            }

            parentGet = $parse(attrs[attrName]);
            scope[scopeName] = function (locals) {
              return parentGet(scope, locals);
            };
          }

          break;
      }
    });

    /**
     * Optional fallback value if attribute is not specified on element
     * @param scopeName
     */
    function bypassWithDefaults(attrName, scopeName, defaultVal) {
      if (!angular.isDefined(attrs[attrName])) {
        var hasDefault = bindDefaults && bindDefaults.hasOwnProperty(scopeName);
        scope[scopeName] = hasDefault ? bindDefaults[scopeName] : defaultVal;
        return true;
      }
      return false;
    }

  };
}

angular.module('material.utils')
  .service('$iterator', IteratorFactory);

/**
 * $iterator Service Class
 */

function IteratorFactory() {

  return function (items, loop) {
    return new List(items, loop);
  };

  /**
   * List facade to easily support iteration and accessors
   * @param items Array list which this iterator will enumerate
   * @param loop Boolean enables iterator to consider the list as an endless loop
   * @constructor
   */
  function List(items, loop) {
    loop = !!loop;

    var _items = items || [ ];

    // Published API

    return {

      items: getItems,
      count: count,

      hasNext: hasNext,
      inRange: inRange,
      contains: contains,
      indexOf: indexOf,
      itemAt: itemAt,
      findBy: findBy,

      add: add,
      remove: remove,

      first: first,
      last: last,
      next: next,
      previous: previous

    };

    /**
     * Publish copy of the enumerable set
     * @returns {Array|*}
     */
    function getItems() {
      return [].concat(_items);
    }

    /**
     * Determine length of the list
     * @returns {Array.length|*|number}
     */
    function count() {
      return _items.length;
    }

    /**
     * Is the index specified valid
     * @param index
     * @returns {Array.length|*|number|boolean}
     */
    function inRange(index) {
      return _items.length && ( index > -1 ) && (index < _items.length );
    }

    /**
     * Can the iterator proceed to the next item in the list; relative to
     * the specified item.
     *
     * @param tab
     * @returns {Array.length|*|number|boolean}
     */
    function hasNext(tab) {
      return tab ? inRange(indexOf(tab) + 1) : false;
    }

    /**
     * Get item at specified index/position
     * @param index
     * @returns {*}
     */
    function itemAt(index) {
      return inRange(index) ? _items[index] : null;
    }

    /**
     * Find all elements matching the key/value pair
     * otherwise return null
     *
     * @param val
     * @param key
     *
     * @return array
     */
    function findBy(key, val) {

      /**
       * Implement of e6 Array::find()
       * @param list
       * @param callback
       * @returns {*}
       */
      function find(list, callback) {
        var results = [ ];

        angular.forEach(list, function (it, index) {
          var val = callback.apply(null, [it, index, list]);
          if (val) {
            results.push(val);
          }
        });

        return results.length ? results : undefined;
      }

      // Use iterator callback to matches element key value
      // NOTE: searches full prototype chain

      return find(_items, function (el) {
        return ( el[key] == val ) ? el : null;
      });

    }

    /**
     * Add item to list
     * @param it
     * @param index
     * @returns {*}
     */
    function add(it, index) {
      if (!angular.isDefined(index)) {
        index = _items.length;
      }

      _items.splice(index, 0, it);

      return indexOf(it);
    }

    /**
     * Remove it from list...
     * @param it
     */
    function remove(it) {
      _items.splice(indexOf(it), 1);
    }

    /**
     * Get the zero-based index of the target tab
     * @param it
     * @returns {*}
     */
    function indexOf(it) {
      return _items.indexOf(it);
    }

    /**
     * Boolean existence check
     * @param it
     * @returns {boolean}
     */
    function contains(it) {
      return it && (indexOf(it) > -1);
    }

    /**
     * Find the next item
     * @param tab
     * @returns {*}
     */
    function next(it, validate) {

      if (contains(it)) {
        var index = indexOf(it) + 1,
          found = inRange(index) ? _items[ index ] :
            loop ? first() : null,
          skip = found && validate && !validate(found);

        return skip ? next(found) : found;
      }

      return null;
    }

    /**
     * Find the previous item
     * @param tab
     * @returns {*}
     */
    function previous(it, validate) {

      if (contains(it)) {
        var index = indexOf(it) - 1,
          found = inRange(index) ? _items[ index ] :
            loop ? last() : null,
          skip = found && validate && !validate(found);

        return skip ? previous(found) : found;
      }

      return null;
    }

    /**
     * Return first item in the list
     * @returns {*}
     */
    function first() {
      return _items.length ? _items[0] : null;
    }

    /**
     * Return last item in the list...
     * @returns {*}
     */
    function last() {
      return _items.length ? _items[_items.length - 1] : null;
    }

  }

}




})();