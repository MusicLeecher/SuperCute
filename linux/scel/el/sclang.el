;; copyright 2003 stefan kersten <steve@k-hornz.de>
;;
;; This program is free software; you can redistribute it and/or
;; modify it under the terms of the GNU General Public License as
;; published by the Free Software Foundation; either version 2 of the
;; License, or (at your option) any later version.
;;
;; This program is distributed in the hope that it will be useful, but
;; WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;; General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with this program; if not, write to the Free Software
;; Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
;; USA

(eval-when-compile (require 'cl))

(defgroup sclang nil
  "IDE for working with the SuperCollider language."
  :group 'languages)

(defgroup sclang-mode nil
  "Major mode for working with SuperCollider source code."
  :group 'sclang)

(defgroup sclang-interface nil
  "Interface to the SuperCollider process."
  :group 'sclang)

(defun sclang-customize ()
  (interactive)
  (customize-group 'sclang))

(eval-when-compile (load "sclang-browser"))
(eval-when-compile (load "sclang-interp"))
(eval-when-compile (load "sclang-language"))
(eval-when-compile (load "sclang-server"))
(eval-when-compile (load "sclang-menu"))
(eval-when-compile (load "sclang-help"))
(eval-when-compile (load "sclang-mode"))

(provide 'sclang)

;; EOF